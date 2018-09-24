#property strict

#define SOCKET_LIBRARY_USE_EVENTS

// --------------------------------------------------------------------
// Include
// --------------------------------------------------------------------
#include "socket-library-mt4-mt5.mqh"
#include "JTradeNewBarDetector.mqh"
#include "JTradeAccontChangesDetector.mqh"


// --------------------------------------------------------------------
// EA user inputs
// --------------------------------------------------------------------
input ushort   ServerPort  = 23456;      // Server port
input int      MagicNumber = 99999999;   // Expert Advisor ID 


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
#define CMD_REGISTER_EA        1  // Comando enviado de EA para EA com objetivo de sincronização
#define CMD_SYMBOL             2  //
#define CMD_BUY                3  // Compra 
#define CMD_SELL               4  // Venda
#define CMD_BUY_LIMIT          5  //
#define CMD_SELL_LIMIT         6  //
#define CMD_BUY_STOP           7  //
#define CMD_SELL_STOP          8  //
#define CMD_MODIFY_POSITION    9  //
#define CMD_MODIFY_ORDER       10 //
#define CMD_REMOVE             11 //
#define CMD_CLOSE              12 //
#define CMD_CLOSE_PARTIAL      13 //


// --------------------------------------------------------------------
// Tópicos de subscrição
// --------------------------------------------------------------------
#define TOPIC_TICK        1  // Todos os ticks
#define TOPIC_RATES       2  // Candles (fechamento de barras)
#define TOPIC_SERVERS     3  // Porta dos outros EA's cadastrados para a mesma conta
#define TOPIC_ACCOUNT     4  // Informações atualizadas sobre a conta de negociação
#define TOPIC_POSITION    5  // Positions, Orders, Deals


/**
 * Representa o tipo de cliente conectado
 */
enum ClientType { CLIENT_TYPE_EA, CLIENT_TYPE_CLIENT, CLIENT_TYPE_UNDEFINED };

/**
 * Os períodos observados por este EA
 */
ENUM_TIMEFRAMES TIMEFRAMES[] = {
   PERIOD_M1,
   PERIOD_M5,
   PERIOD_M15,
   PERIOD_M30,
   PERIOD_H1,
   PERIOD_H4,
   PERIOD_D1,
   PERIOD_W1,
   PERIOD_MN1
};

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

CTrade  trade;

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

NewBarDetector *bars[ArraySize(TIMEFRAMES)];

AccontChangesDetector accontChange = new AccontChangesDetector();


/**
 * Inicialização
 *
 * 1 - Verifi
 */
void OnInit() {


   trade.SetAsyncMode(true);
   trade.SetExpertMagicNumber(MagicNumber);

   for(int i = ArraySize(TIMEFRAMES) - 1; i >= 0; i--) {
      bars[i] = new NewBarDetector(TIMEFRAMES[i]);
   }      
   
   // Verifica se já existe um MASTER
   glbClientSocket = new ClientSocket("127.0.0.1", ServerPort);
   if (glbClientSocket.IsSocketConnected()) {
      // Aguarda mensagem do server determinando a porta que deve ser usada neste server
      Print("JTrade: Client connection succeeded");
      
      // @TODO: Subsrever no tópico SERVERS
   } else {
      // Criar o Master
      ushort serverPort = ServerPort;
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
   MqlRates rate;
   MqlRates rates[];   
   int countNewBars;
   
   if(SymbolInfoTick(Symbol(), tick)) {
   
      if (accontChange.hasChange()){
         // Envia informações sobre o novo estado da conta
         publishAccount();
      }

      // Envia tick            
      publishTick(tick.time_msc, tick.bid, tick.ask, tick.last, tick.volume);

      // Verificar por fechamento de candles
      for (int i = ArraySize(TIMEFRAMES) - 1; i >= 0; i--) {
         countNewBars = bars[i].countNewBar();
         if (countNewBars > 0){
            ArraySetAsSeries(rates, true);
            int copied = CopyRates(Symbol(), TIMEFRAMES[i], 0, countNewBars, rates);
            if ( copied > 0 ){
               for(int j = 0; j < copied; j++){
                  rate = rates[j];
                  publishRate(rate.time, rate.open, rate.high, rate.low, rate.close, rate.tick_volume, rate.real_volume, rate.spread, PeriodSeconds(TIMEFRAMES[i]));
               }
            }
         }
      }
   } 
}


/**
 * Function-event handler "trade"
 */
void OnTrade(void) {
   // @TODO: Evitar publicação desnecessária, tratar quando fazer a publicação
   publishPosition();
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

   if (hasNewClient) {
      // Envia imediatamente as portas com as conexões para outros gráficos      
      publishServers();
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
      
      if (StringFind(line, "*") == 0){
         // Está respondendo a um topico, no formato "*<ID_TOPICO>*<CONTEUDO>"
         k = StringSplit(line, StringGetCharacter("*", 0), parts);         
         if (k != 3) {
             // Formato de mensagem inválida
             continue;
         }
         
         int serverPort;
         switch((int) StringToInteger(parts[1])){
            // Listagem das portas de todos os outros servers (EA's) abertos 
            // No formato: "SERVERS_<PORT_1>_<PORT_2>_<PORT_N>"
            case TOPIC_SERVERS:
              
               // Redimensiona a lista de servers
               k = StringSplit(parts[2], StringGetCharacter("_", 0), parts);
               ArrayResize(glbServers, k-1);
               
               serverPort = ServerPort;
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
               break;
         }         
      } else {
         k = StringSplit(line, StringGetCharacter("_", 0), parts);
         if(k > 0) {
            request = parts[0];
            
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
                  
                  // Para alguns tópicos, já envia a resposta imediatamente
                  if(topic == TOPIC_ACCOUNT){                     
                     publishAccount();
                  } else if(topic == TOPIC_POSITION){
                     publishPosition();          
                  }                 
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
               double price;
               double volume; 
               long deviation; 
               double sl;
               double tp;
   
               switch(command) {
                  case CMD_REGISTER_EA:
                     // "<REQUEST_ID>_<CMD_REGISTER_EA>_<PORT_NUMBER>"
                     comandRegisterEA(client, requestId, StringToInteger(parts[2]));
                     break;
                  case CMD_SYMBOL:                      
                     comandSymbol(client, requestId);
                     break;
                  case CMD_BUY:
                     price = StringToDouble(parts[2]);
                     volume = StringToDouble(parts[3]); 
                     deviation = StringToInteger(parts[4]); 
                     sl = StringToDouble(parts[5]);
                     tp = StringToDouble(parts[6]);
                     comandBuy(client, requestId, price, volume, deviation, sl, tp);
                     break;
                  case CMD_SELL:
                     price = StringToDouble(parts[2]);
                     volume = StringToDouble(parts[3]); 
                     deviation = StringToInteger(parts[4]); 
                     sl = StringToDouble(parts[5]);
                     tp = StringToDouble(parts[6]);
                     comandSell(client, requestId, price, volume, deviation, sl, tp);
                     break;
                                          
               } 
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
 * Publica o Tick atual para os interessados
 * 
 * @param timems Time of a price last update in milliseconds 
 * @param last Price of the last deal (Last) 
 * @param volume  Volume for the current Last price 
 * @param isBuy 1: a tick is a result of a buy deal, -1: a tick is a result of a sell deal, 0: otherwise
 */
void publishTick(long timeMilis, double bid, double ask, double last, ulong volume){

   // "SYMBOL TIME BID ASK LAST VOLUME"
   string content = Symbol()
         + " " + timeMilis
         + " " + DoubleToString(bid) 
         + " " + DoubleToString(ask) 
         + " " + DoubleToString(last) 
         + " " + IntegerToString(volume);

   publishOnTopic(TOPIC_TICK, content);
}


/**
 * Publica o fechamento de candles para o timeframe informado
 */
void publishRate(datetime timeSeconds, double open, double hight, double low, double close, ulong volumeTick, ulong volumeReal, int spread, int timeframe){

   // "SYMBOL TIME OPEN HIGH LOW CLOSE TICK_VOLUME REAL_VOLUME SPREAD INTERVAL"
   string content = Symbol()
         + " " + IntegerToString((uint) timeSeconds)
         + " " + DoubleToString(open) 
         + " " + DoubleToString(hight) 
         + " " + DoubleToString(low) 
         + " " + DoubleToString(close) 
         + " " + IntegerToString(volumeTick)
         + " " + IntegerToString(volumeReal)
         + " " + IntegerToString(spread) 
         + " " + IntegerToString(timeframe);

   publishOnTopic(TOPIC_RATES, content);
}


/**
 * Envia a lista com as portas dos servidores para todos os clientes
 */
void publishServers(){

   // "SYMBOL TIME BID ASK LAST VOLUME"           
   string content = IntegerToString(glbServerPort);
   StringAdd(content, "_");
      
   for (int sz = ArraySize(glbClients), i = sz - 1; i >= 0; i--) {
      Client client = glbClients[i];
      if(client.type == CLIENT_TYPE_EA){
         StringAdd(content, IntegerToString(client.port));
         if(i < sz-1) {
            StringAdd(content, ",");
         }
      }
   }

   publishOnTopic(TOPIC_SERVERS, content);
}


/**
 * Publica o fechamento de candles para o timeframe informado
 */
void publishAccount(){

   // "TIME CURRENCY LEVERAGE BALANCE EQUITY MARGIN MARGIN_FREE PROFIT"
   
   string content = IntegerToString((uint) TimeCurrent())
         + " " + AccountInfoString(ACCOUNT_CURRENCY) 
         + " " + IntegerToString(AccountInfoInteger(ACCOUNT_LEVERAGE))
         + " " + DoubleToString(AccountInfoDouble(ACCOUNT_BALANCE))
         + " " + DoubleToString(AccountInfoDouble(ACCOUNT_EQUITY))
         + " " + DoubleToString(AccountInfoDouble(ACCOUNT_MARGIN))
         + " " + DoubleToString(AccountInfoDouble(ACCOUNT_MARGIN_FREE))
         + " " + DoubleToString(AccountInfoDouble(ACCOUNT_PROFIT))
         ;

   publishOnTopic(TOPIC_ACCOUNT, content);
}



/**
 * Publica as posições abertas
 */
void publishPosition(){

   // "<SYMBOL>|<POSITION><POSITION><POSITION>|<ORDER><ORDER><ORDER>|<DEAL><DEAL><DEAL>"
   
  
   string content = Symbol();
   
   // Separador SYMBOL|POSITIONS|ORDERS|DEALS
   StringAdd(content, "|");
   
   int positions = PositionsTotal();
   for ( int i=0 ; i < positions ; i++ ) {
      ResetLastError();
      
      string symbol = PositionGetSymbol(i);
      if (symbol != Symbol())  {
         continue;
      }
      
      // POSITION: "TIME_MSC IDENTIFIER TYPE PRICE_OPEN VOLUME SL TP"   
      
      StringAdd(content, IntegerToString(PositionGetInteger(POSITION_TIME_MSC)) + " ");
      StringAdd(content, IntegerToString(PositionGetInteger(POSITION_IDENTIFIER)) + " ");
      StringAdd(content, IntegerToString(PositionGetInteger(POSITION_TYPE)) + " ");
      
      StringAdd(content, DoubleToString(PositionGetDouble(POSITION_PRICE_OPEN)) + " ");
      StringAdd(content, DoubleToString(PositionGetDouble(POSITION_VOLUME)) + " ");
      StringAdd(content, DoubleToString(PositionGetDouble(POSITION_SL)) + " ");
      StringAdd(content, DoubleToString(PositionGetDouble(POSITION_TP)) + " ");
   }
   
   // Separador SYMBOL|POSITIONS|ORDERS|DEALS
   StringAdd(content, "|");
   
   int orders = HistoryOrdersTotal();
   for ( int i=0; i < orders ; i++ ) {
      ResetLastError();
      
      ulong ticket = OrderGetTicket(i);      
      if(ticket == 0) {
         continue;
      }
      
      string symbol = OrderGetString(ORDER_SYMBOL);
      if (symbol != Symbol())  {
         continue;
      }
      
      // ORDER: "TIME_MSC TICKET POSITION TYPE STATE FILLING PRICE VOLUME SL TP STOPLIMIT"
      
      int time = OrderGetInteger(ORDER_TIME_DONE_MSC);
      if(time == 0){
         time = OrderGetInteger(ORDER_TIME_SETUP_MSC);
      }
      
      StringAdd(content, IntegerToString(time) + " ");
      StringAdd(content, IntegerToString(OrderGetInteger(ORDER_TICKET)) + " ");
      StringAdd(content, IntegerToString(OrderGetInteger(ORDER_POSITION_ID)) + " ");
      StringAdd(content, IntegerToString(OrderGetInteger(ORDER_TYPE)) + " ");
      StringAdd(content, IntegerToString(OrderGetInteger(ORDER_STATE)) + " ");
      StringAdd(content, IntegerToString(OrderGetInteger(ORDER_TYPE_FILLING)) + " ");
      
      StringAdd(content, DoubleToString(OrderGetDouble(ORDER_PRICE_CURRENT)) + " ");
      StringAdd(content, DoubleToString(OrderGetDouble(ORDER_VOLUME_CURRENT)) + " ");
      StringAdd(content, DoubleToString(OrderGetDouble(ORDER_SL)) + " ");
      StringAdd(content, DoubleToString(OrderGetDouble(ORDER_TP)) + " ");
      StringAdd(content, DoubleToString(OrderGetDouble(ORDER_PRICE_STOPLIMIT)) + " ");
                    
   }

   // Separador SYMBOL|POSITIONS|ORDERS|DEALS
   StringAdd(content, "|");
   
   for ( int i=0 ; i < positions ; i++ ) {
      ResetLastError();
      
      string symbol = PositionGetSymbol(i);
      if (symbol != Symbol())  {
         continue;
      }
      
      if(!HistorySelectByPosition(PositionGetInteger(POSITION_IDENTIFIER))) {
         continue;   
      }
      
      int deals = HistoryDealsTotal();
      for ( int j=0 ; j < deals ; j++ ) {
         ResetLastError();
         
         ulong dealTicket = HistoryDealGetTicket(i);      
         if(dealTicket == 0) {
            continue;
         }
         
         // DEAL: "TIME_MSC TICKET ORDER POSITION TYPE ENTRY PRICE VOLUME COMMISSION SWAP PROFIT"
         
         StringAdd(content, IntegerToString(HistoryDealGetInteger(dealTicket, DEAL_TIME_MSC)) + " ");
         StringAdd(content, IntegerToString(HistoryDealGetInteger(dealTicket, DEAL_TICKET)) + " ");
         StringAdd(content, IntegerToString(HistoryDealGetInteger(dealTicket, DEAL_ORDER)) + " ");
         StringAdd(content, IntegerToString(HistoryDealGetInteger(dealTicket, DEAL_POSITION_ID)) + " ");
         StringAdd(content, IntegerToString(HistoryDealGetInteger(dealTicket, DEAL_TYPE)) + " ");
         StringAdd(content, IntegerToString(HistoryDealGetInteger(dealTicket, DEAL_ENTRY)) + " ");
         
         StringAdd(content, DoubleToString(HistoryDealGetDouble(dealTicket, DEAL_PRICE)) + " ");
         StringAdd(content, DoubleToString(HistoryDealGetDouble(dealTicket, DEAL_VOLUME)) + " ");
         StringAdd(content, DoubleToString(HistoryDealGetDouble(dealTicket, DEAL_COMMISSION)) + " ");
         StringAdd(content, DoubleToString(HistoryDealGetDouble(dealTicket, DEAL_SWAP)) + " ");
         StringAdd(content, DoubleToString(HistoryDealGetDouble(dealTicket, DEAL_PROFIT)) + " ");
      }        
   }
   
   publishOnTopic(TOPIC_POSITION, content);
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
      
   publishServers();   
}


/**
 * Envia para o cliente as informações sobre o símbolo que este EA está atuando
 */
void comandSymbol(Client& client, int requestId){
   // "SYMBOL BASE QUOTE DIGITS CONTRACT_SIZE TICK_VALUE TIME BID ASK STOPS_LEVEL FREEZE_LEVEL"
   string symbol = Symbol();
   string content = symbol
         + " " + SymbolInfoString(symbol, SYMBOL_CURRENCY_BASE)
         + " " + SymbolInfoString(symbol, SYMBOL_CURRENCY_PROFIT)
         + " " + IntegerToString(SymbolInfoInteger(symbol, SYMBOL_DIGITS))
         + " " + DoubleToString(SymbolInfoDouble(symbol, SYMBOL_TRADE_CONTRACT_SIZE)) 
         + " " + DoubleToString(SymbolInfoDouble(symbol, SYMBOL_TRADE_TICK_VALUE)) 
         + " " + IntegerToString((uint) TimeCurrent())
         + " " + DoubleToString(SymbolInfoDouble(symbol, SYMBOL_BID))         
         + " " + DoubleToString(SymbolInfoDouble(symbol, SYMBOL_ASK))
         + " " + IntegerToString(SymbolInfoInteger(symbol, SYMBOL_TRADE_STOPS_LEVEL))
         + " " + IntegerToString(SymbolInfoInteger(symbol, SYMBOL_TRADE_FREEZE_LEVEL))
         ;
   
   Print(content);
   commandSendResponse(client, requestId, 0, content);
}


void comandBuy(Client& client, int requestId, double price, double volume, long deviation, double sl, double tp){
   
   string content = "";
   int error = 0;
   trade.SetDeviationInPoints(deviation);

   // @TODO Permitir outros tipos
   trade.SetTypeFilling(ORDER_FILLING_FOK);

   if(!trade.Buy(volume, Symbol(), price, sl, tp)){
      error = trade.ResultRetcode();      
   }

   commandSendResponse(client, requestId, error, content);
}


void comandSell(Client& client, int requestId, double price, double volume, long deviation, double sl, double tp){

   string content = "";
   int error = 0;
   trade.SetDeviationInPoints(deviation);

   // @TODO Permitir outros tipos
   trade.SetTypeFilling(ORDER_FILLING_FOK);

   if(!trade.Sell(volume, Symbol(), price, sl, tp)){
      error = trade.ResultRetcode();      
   }

   commandSendResponse(client, requestId, error, content);
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
   
   Print(message);
   client.socket.Send(message);
}
