#property strict

// --------------------------------------------------------------------
// Include socket library, asking for event handling
// --------------------------------------------------------------------
#define SOCKET_LIBRARY_USE_EVENTS
#include "socket-library-mt4-mt5.mqh"

// --------------------------------------------------------------------
// EA user inputs
// --------------------------------------------------------------------
input ushort   ServerPort = 23456;  // Server port


// --------------------------------------------------------------------
// Global variables and constants
// --------------------------------------------------------------------

/**
 * Freqüência para EventSetMillisecondTimer (). Não precisa
 * ser muito frequente, porque é apenas um backup para o OnChartEvent()
 */
#define TIMER_FREQUENCY_MS    1000
#define CRLF                  "\r\n"

// --------------------------------------------------------------------
// Comandos recebidos do cliente
// 
// No formato:
// "<NUM_REQUISICAO>_<COD_COMANDO>_<PARAM_1>_<PARAM_2>_<PARAM_N>"
// --------------------------------------------------------------------
#define CMD_REGISTER_EA    1  // Comando enviado de EA para EA com objetivo de sincronização
#define CMD_XPTO           2


// --------------------------------------------------------------------
// Tópicos de subscrição
// --------------------------------------------------------------------
#define TOPIC_TICK        1  // Todos os ticks
#define TOPIC_RATES       2  // Candles (fechamento de barras)


/**
 * Representa o tipo de cliente conectado
 */
enum ClientType { CLIENT_TYPE_EA, CLIENT_TYPE_CLIENT, CLIENT_TYPE_UNDEFINED };

/**
 * Representa uma conexão cliente
 *
 * Um cliente pode ser um outro server ou o próprio backend em Java da solução JTrade
 */
class Client { 
   public:
      // Número da porta, quando for outro server
      int port;
      // Tópicos que o cliente está subscrito
      int topics[];
      // O tipo de cliente conectado
      ClientType type;
      // A conexão com o cliente
      ClientSocket * socket;
};

/**
 * O número da porta onde este EA está servindo
 */
int glbServerPort;

/**
 * Socket Server
 *
 * Todo o EA prove um socket, para permitir a conexão com o backend em Java.
 * 
 * Existe apenas um EA master, que roda na "ServerPort"
 */
ServerSocket * glbServerSocket = NULL;

/**
 * Socket Cliente
 *
 * Durante a criação do E.A, ele conecta-se ao primeiro EA criado e informa qual a porta que foi inicializado.
 */
ClientSocket * glbClientSocket = NULL;

/**
 * A lista de clientes deste E.A
 */
Client glbClients[];

/**
 * A lista com as portas dos outros servers (EA)
 *
 * O EA JTrade deve ser adicionado a cada gráfico desejado. 
 * Esse é o mecanismo de controle para permitir a sincronização entre os graficos.
 *
 * Se o MASTER cair, o server com a porta mais baixa vai tentar assumir o controle como master
 */
int glbServers[];

/**
 * Verifica a necessidade de criar temporizador
 */
bool glbCreatedTimer = false;


/**
 * Inicialização
 *
 * 1 - Verifi
 */
void OnInit() {
   
   // Verifica se já existe um MASTER
   glbClientSocket = new ClientSocket("127.0.0.1", ServerPort);
   if (glbClientSocket.IsSocketConnected()) {
      // Aguarda mensagem do server determinando a porta que deve ser usada neste server
      Print("JTrade: Client connection succeeded");
   } else {
      // Criar o Master
      int serverPort = ServerPort;
      while(!createServer(serverPort++)){
         continue;
      }      
   }
}


/**
 * Termination
 *
 * Limpa referencias do server e dos clientes
 *
 * IMPORTANTE! Deve remover as conexões ou então a porta ficará ocupada até a finalização do Metatrader
 */
void OnDeinit(const int reason) {
   glbCreatedTimer = false;
   
   // Excluir todos os clientes conectados
   ArrayFree(glbClients);
   
   // Limpa o socket cliente
   if (glbClientSocket) {
      delete glbClientSocket;
      glbClientSocket = NULL;
   }

   // Limpa o socket servidor
   if(glbServerSocket){
      delete glbServerSocket;
      glbServerSocket = NULL;
   }
   
   Print("JTrade: Server socket terminated");
}


/**
 * Use OnTick() to watch for failure to create the timer in OnInit()
 */
void OnTick() {
   if (!glbCreatedTimer) glbCreatedTimer = EventSetMillisecondTimer(TIMER_FREQUENCY_MS);

   MqlTick tick;
   if(SymbolInfoTick(Symbol(), tick)) {
            
      publishTick(tick.time_msc, tick.bid, tick.ask, tick.last, tick.volume);

      // @TODO: Verificar por fechamento de candles
   } 
}


/**
 * Function-event handler "trade"
 */
void OnTrade(void) {
   // 
}


/**
 * Timer - accept new connections, and handle incoming data from clients.
 * Secondary to the event-driven handling via OnChartEvent(). Most
 * socket events should be picked up faster through OnChartEvent()
 * rather than being first detected in OnTimer()
 */
void OnTimer() {
   // Aceita todas as novas conexões pendentes
   acceptNewConnections();
   
   // Processar quaisquer dados recebidos em cada soquete do cliente
   // tendo em mente que o handleSocketIncomingData()
   // pode excluir sockets e reduzir o tamnaho do array
   // se um socket foi fechado
   for (int i = ArraySize(glbClients) - 1; i >= 0; i--) {
      handleSocketIncomingData(i);
   }
}


// --------------------------------------------------------------------
// Event-driven functionality, turned on by #defining SOCKET_LIBRARY_USE_EVENTS
// before including the socket library. This generates dummy key-down
// messages when socket activity occurs, with lparam being the 
// .GetSocketHandle()
// --------------------------------------------------------------------
void OnChartEvent(const int id, const long& lparam, const double& dparam, const string& sparam) {
   if (id == CHARTEVENT_KEYDOWN) {
      // If the lparam matches a .GetSocketHandle(), then it's a dummy
      // key press indicating that there's socket activity. Otherwise,
      // it's a real key press
         
      if (lparam == glbServerSocket.GetSocketHandle()) {
         // Activity on server socket. Accept new connections
         Print("JTrade:New server socket event - incoming connection");
         acceptNewConnections();

      } else {
         // Compare lparam to each client socket handle
         for (int i = 0; i < ArraySize(glbClients); i++) {
            if (lparam == glbClients[i].socket.GetSocketHandle()) {
               handleSocketIncomingData(i);
               return; // Early exit
            }
         }
         
         // If we get here, then the key press does not seem
         // to match any socket, and appears to be a real
         // key press event...
      }
   }
}


/**
 * Aceita novas conexões no Socket server, criando novas entradas no array glbClients []
 */
void acceptNewConnections() {
   // Continue aceitando todas as conexões pendentes até que Accept() retorne NULL
   
   bool hasNewClient = false;

   while(true) {    
      Client client;  
      client.socket = glbServerSocket.Accept();
      client.type = CLIENT_TYPE_UNDEFINED;
      if (client.socket != NULL) {
         hasNewClient = true;

         int sz = ArraySize(glbClients);
         ArrayResize(glbClients, sz + 1);
         glbClients[sz] = client;
         
         Print("JTrade: New client connection");
      } else {
         break;
      }      
   };

   if(hasNewClient){
      // Envia imediatamente as portas para a conexão com outros gráficos      
      sendServersToAll();
   }
}


/**
 * Lida com qualquer novo dado recebido em um soquete do cliente, identificado
 * pelo seu índice dentro do array glbClients []. Esta função
 * exclui o objeto ClientSocket e reestrutura o array,
 * se o socket foi fechado pelo cliente
 */
void handleSocketIncomingData(int idxClient) {
   Client client = glbClients[idxClient];

   // Cliente enviou a mensagem "CLOSE"
   bool bForceClose = false;
   string line;
   string parts[];   
   string request;
   int k;
   do {
      
      line = client.socket.Receive(CRLF);               
      k = StringSplit(line, StringGetCharacter("_", 0), parts); 
      if(k > 0) {
         request = parts[0];
         
         // if (command == "quote") {
         //    client.socket.Send(Symbol() + "," + DoubleToString(SymbolInfoDouble(Symbol(), SYMBOL_BID), 6) + "," + DoubleToString(SymbolInfoDouble(Symbol(), SYMBOL_ASK), 6) + CRLF);
   
         // }
         
         // Permite ao cliente subscrever ou remover a subscrição em um tópico, no formato "TOPIC_<CODIGO_TOPICO>_<0|1>"
         // Ex. TOPIC_1_1, TOPIC_3_1, 
         if (request == "TOPIC") {
            if(k == 3){
               int topic = StringToInteger(parts[1]);               
               bool subscribe = (parts[2] == "1");

               // Reordena os tópicos do cliente antes 
               ArraySort(client.topics);

               int sz = ArraySize(client.topics);
               // Já está subscrito no topico
               bool isSubscribed = ArrayBsearch(client.topics, topic) >= 0;
               
               int topics[];
               ArrayResize(topics, sz);                     
               ArrayCopy(topics, client.topics);

               if(subscribe){
                  // Subscrevendo no tópico
                  if (!isSubscribed) {
                     // Cliente ainda não está subscrito no tópico
                     
                     ArrayResize(topics, sz+1);                     
                     topics[sz] = topic;                                         
                  }
               } else{
                  // Removendo subscrição no tópico
                  if (isSubscribed) {
                     
                     int index = ArrayBsearch(topics, topic);
                     ArrayResize(topics, sz-1);

                     for(int i=0, j=0; i < sz; i++, j++) {
                        if (i == index) {
                           j--;
                        } else {
                           topics[j] = client.topics[i];
                        }
                     }                     
                  }
               }
   
               ArraySort(topics);
               ArrayResize(glbClients[idxClient].topics, ArraySize(topics));
               ArrayCopy(glbClients[idxClient].topics, topics);
               // Reordena a lista de tópicos
               
            }            
         }         
         // Listagem das portas de todos os outros servers (EA's) abertos 
         // No formato: "SERVERS_<PORT_1>_<PORT_2>_<PORT_N>"
         else if (request == "SERVERS") {                                   
            
            // Redimensiona a lista de servers
            ArrayResize(glbServers, k-1);
            
            int serverPort = ServerPort;
            if(k > 1) {
               for(int i=0, j=1; j < k; i++, j++) {
                  glbServers[i] = StringToInteger(parts[j]);
                  serverPort = MathMax(serverPort, glbServers[i]);
               }
            }
            
            // Inicializa o server, se necessário e salva o número da porta 
            if(!glbServerSocket) {
               while(!createServer(++serverPort)){
                  continue;
               }               
               // Informa ao MASTER sobre a porta deste EA
               client.socket.Send("0_" + CMD_REGISTER_EA + "_" + IntegerToString(serverPort) + CRLF);
            }
         }
         else if (request == "CLOSE") {            
            bForceClose = true;
            
         } 
         // RPC
         // No formato: "<REQUEST_ID>_<COMMAND_CODE>_<PARAM_1>_<PARAM_2>_<PARAM_N>"
         else if (k > 1){          
            int requestId = StringToInteger(parts[0]);
            int command = StringToInteger(parts[1]);

            switch(command) {
               case CMD_REGISTER_EA:
                  // "<REQUEST_ID>_<CMD_REGISTER_EA>_<PORT_NUMBER>"
                  comandRegisterEA(client, requestId, StringToInteger(parts[2]));
                  break;
               case CMD_XPTO: 
                  comandXpto(client, requestId);
                  break;
            } 
         }
      }      
   } while (line != "");

   // If the socket has been closed, or the client has sent a close message,
   // release the socket and shuffle the glbClients[] array
   if (!client.socket.IsSocketConnected() || bForceClose) {
      Print("JTrade: Client has disconnected");
      
      if(glbClientSocket == client.socket){
         // @TODO: Reconectar-se ao server?
         delete glbClientSocket;
      }

      // Cliente está morto. Destroi o objeto
      // delete client;
      
      // Remove do array
      int ctClients = ArraySize(glbClients);
      for (int i = idxClient + 1; i < ctClients; i++) {
         glbClients[i - 1] = glbClients[i];
      }
      ctClients--;
      ArrayResize(glbClients, ctClients);
      
     
   }
}


/**
 * Inicializa o server na porta especificada
 */
bool createServer(ushort port){
      
   glbServerSocket = new ServerSocket(port, false);
   if (glbServerSocket.Created()) {
   
      // Salva o número da porta
      glbServerPort = port;
      
      // Adiciona a porta na lista de servers
      int sz = ArraySize(glbServers);
      ArrayResize(glbServers, sz + 1);
      glbServers[sz] = glbServerPort;
      
      Print("JTrade: Server socket created");

      // Note: this can fail if MT4/5 starts up
      // with the EA already attached to a chart. Therefore,
      // we repeat in OnTick()
      glbCreatedTimer = EventSetMillisecondTimer(TIMER_FREQUENCY_MS);
      
      return true;
   } else {
      Print("JTrade: Server socket FAILED - is the port already in use?");
      
      return false;
   }
}


// --------------------------------------------------------------------
// Tópicos (PUB SUB)
// --------------------------------------------------------------------



/**
 * Envia a lista com as portas dos servidores para todos os clientes
 */
void sendServersToAll() {
   
   string message = "SERVERS_";
   StringAdd(message, IntegerToString(glbServerPort));
   StringAdd(message, ",");
      
   for (int sz = ArraySize(glbClients), i = sz - 1; i >= 0; i--) {
      Client client = glbClients[i];
      if(client.type == CLIENT_TYPE_EA){
         StringAdd(message, IntegerToString(client.port));
         if(i < sz-1) {
            StringAdd(message, ",");
         }
      }
   }
   StringAdd(message, CRLF);    

   for (int i = ArraySize(glbClients) - 1; i >= 0; i--) {
      glbClients[i].socket.Send(message);
   }
}


/**
 * Publica o Tick atual para os interessados
 * 
 * @param timems Time of a price last update in milliseconds 
 * @param last Price of the last deal (Last) 
 * @param volume  Volume for the current Last price 
 * @param isBuy 1: a tick is a result of a buy deal, -1: a tick is a result of a sell deal, 0: otherwise
 */
void publishTick(long timems, double bid, double ask, double last, ulong volume){

   // "SYMBOL TIME BID ASK LAST VOLUME"
   string content = Symbol()
         + " " + timems
         + " " + DoubleToString(bid) 
         + " " + DoubleToString(ask) 
         + " " + DoubleToString(last) 
         + " " + IntegerToString(volume);

   publishOnTopic(TOPIC_TICK, content);
}


/**
 * Publica o fechamento de candles para o timeframe informado
 */
void publishRate(long timems, double open, double hight, double low, double close, ulong volumeTick, ulong volumeReal, double spread, int timeframe){

   // "SYMBOL TIME OPEN HIGH LOW CLOSE TICK_VOLUME REAL_VOLUME SPREAD INTERVAL"
   string content = Symbol()
         + " " + timems
         + " " + DoubleToString(open) 
         + " " + DoubleToString(hight) 
         + " " + DoubleToString(low) 
         + " " + DoubleToString(close) 
         + " " + IntegerToString(volumeTick)
         + " " + IntegerToString(volumeReal)
         + " " + DoubleToString(spread) 
         + " " + IntegerToString(timeframe);

   publishOnTopic(TOPIC_RATES, content);
}


// --------------------------------------------------------------------
// Comandos (RPC)
// --------------------------------------------------------------------


/**
 * Permite a um EA informar ao MASTER a porta a que ele está atendendo
 */
void comandRegisterEA(Client& client, int requestId, int portNumber){
   // Esse cliente é um EA   
   client.type = CLIENT_TYPE_EA;
   client.port = portNumber;
      
   sendServersToAll();   
}


/**
 * Executa o comando e evia a reposta para o cliente informado
 */
void comandXpto(Client& client, int requestId){
   commandSendResponse(client, requestId, 0, "Mensagem de  teste qualquer");
}


// --------------------------------------------------------------------
// Métodos utilitários
// --------------------------------------------------------------------


/**
 * Publica o conteúdo no tópico especificado
 */
void publishOnTopic(int topic, string content){
   string message = "*" + IntegerToString(topic) + "*" + content + CRLF;
   for (int i = ArraySize(glbClients) - 1; i >= 0; i--) {
      if(ArrayBsearch(glbClients[i].topics, topic) >= 0){
         // Se o cliente está subscrito no tópico
         glbClients[i].socket.Send(message);
      };
   }
}


/**
 * Envia a resposta para um comando, no formato: "#ID_REQUISICAO#<CONTEUDO>"
 * 
 * Onde <CONTEUDO> pode ser o conteúdo do comando ou "@<CODIGO_ERRO>" em caso de falha
 */
void commandSendResponse(Client& client, int requestId, int error, string content){
   string message = "#" + requestId + "#";

   if (error > 0) {
      StringAdd(message, "@" + error);
   } else {
      StringAdd(message, content);
   }

   StringAdd(message, CRLF);

   client.socket.Send(message);
}


//+------------------------------------------------------------------+
//|                                                Lib CisNewBar.mqh |
//|                                            Copyright 2010, Lizar |
//|                                               Lizar-2010@mail.ru |
//|                                              Revision 2010.09.27 |
//|                             https://www.mql5.com/en/articles/159 |
//+------------------------------------------------------------------+

class NewBar {
   protected:
      datetime          m_lastbar_time;   // Time of opening last bar
      ENUM_TIMEFRAMES   m_period;         // Chart period
      uint              m_retcode;        // Result code of detecting new bar 
      int               m_new_bars;       // Number of new bars
      
   public:
      void              NewBar( ENUM_TIMEFRAMES period );     
      int               countNewBar();
      bool              isNewBar(datetime new_Time);
      uint              getRetCode() const { return m_retcode; }
      datetime          getLastBarTime() const { return m_lastbar_time; }
      int               getNewBars() const { return m_new_bars; }
};

void NewBar::NewBar(ENUM_TIMEFRAMES period) {
   m_retcode = 0; 
   m_new_bars = 0;
   m_lastbar_time = 0;
   m_period = period;
}

/**
 * Obtém a quantidade de novas barras
 * 
 * @return Number of new bars
 */
int NewBar::countNewBar() {
   datetime newbar_time;
   datetime lastbar_time = m_lastbar_time;
      
   // Set value of predefined variable _LastError as 0.
   ResetLastError(); 

   // Request time of opening last bar:
   if ( !SeriesInfoInteger(Symbol(), m_period, SERIES_LASTBAR_DATE, newbar_time) ) { 
      
      // If request has failed, print error message:
      // Result code of detecting new bar: write value of variable _LastError
      m_retcode = GetLastError();  
      return 0;
   }
   
   // Next use first type of request for new bar, to complete analysis:
   if ( !isNewBar(newbar_time) ) {
      return 0;
   }
   
   // Correct number of new bars:
   m_new_bars = Bars(Symbol(), m_period, lastbar_time, newbar_time) - 1;

   // If we've reached this line - then there is(are) new bar(s), return their number:
   return m_new_bars;
}

//+------------------------------------------------------------------+
//| First type of request for new bar                                |
//| INPUT:  newbar_time - time of opening (hypothetically) new bar   |
//| OUTPUT: true   - if new bar(s) has(ve) appeared                  |
//|         false  - if there is no new bar or in case of error      |
//| REMARK: no.                                                      |
//+------------------------------------------------------------------+
/**
 * Verifica se o datetime informado é uma nova barra
 */
bool NewBar::isNewBar(datetime newbar_time) {
   // Initialization of protected variables
   m_new_bars = 0;      // Number of new bars
   m_retcode  = 0;      // Result code of detecting new bar: 0 - no error
   
   // Just to be sure, check: is the time of (hypothetically) new bar m_newbar_time less than time of last bar m_lastbar_time? 
   // If new bar is older than last bar, print error message
   if ( m_lastbar_time > newbar_time ) {
      // Result code of detecting new bar: return -1 - synchronization error
      m_retcode = -1;     
      return false;
   }
        
   // if it's the first call 
   if ( m_lastbar_time == 0 ) {  
      // set time of last bar and exit
      m_lastbar_time = newbar_time; 
      return false;
   }   

   // Check for new bar: 
   if ( m_lastbar_time < newbar_time ) { 
      // Number of new bars
      m_new_bars =1;

      // remember time of last bar 
      m_lastbar_time = newbar_time; 
      return true;
   }
   
   // if we've reached this line, then the bar is not new; return false
   return false;
}


  
