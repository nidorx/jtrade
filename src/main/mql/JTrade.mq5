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

/**
 * Socket Server
 *
 * Todo o EA prove um socket, para permitir a conexão com o backend em Java.
 * 
 * Existe apenas um EA master, que roda na "ServerPort"
 */
ServerSocket * glbServerSocket = NULL;

/**
 * O número da porta onde este EA está servindo
 */
int glbServerPort;

/**
 * Socket Cliente
 *
 * Durante a criação do E.A, ele conecta-se ao primeiro EA criado é informa qual a porta que foi inicializado.
 *
 */
ClientSocket * glbClientSocket = NULL;

/**
 * Representa o tipo de cliente conectado
 */
enum ClientType { EA, CLIENT, UNDEFINED};

/**
 * Representa uma conexão cliente
 *
 * Um cliente pode ser um outro server ou o próprio backend em Java da solução JTrade
 */
struct Client { 
   // Número da porta, quando for outro server
   int port;
   // O tipo de cliente conectado
   ClientType type;
   // A conexão com o cliente
   ClientSocket * socket;
};

/**
 * A lista de clientes deste E.A
 */
Client glbClients[];

/**
 * A lista com as portas dos outros servers (EA)
 *
 * O EA JTrade deve ser adicionado a cada gráfico desejado. 
 * Esse é o mecanismo de controle para permitir a sincronização entre os graficos
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
      createServer(ServerPort);
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
   
   // Processar mensagens provenientes do MASTER
   handleIncomingFromMaster();
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
         Print("New server socket event - incoming connection");
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
   
   while(true) {    
      Client client;  
      client.socket = glbServerSocket.Accept();
      client.type = UNDEFINED;
      if (client.socket != NULL) {
         int sz = ArraySize(glbClients);
         ArrayResize(glbClients, sz + 1);
         glbClients[sz] = client;
         
         Print("JTrade: New client connection");
         
         // Envia imediatamente as portas para a conexão com outros gráficos         
         sendServers(client);
      } else {
          break;
      }      
   };
}


/**
 * Envia a lista com as portas dos servidores para um cliente
 */
void sendServers(Client& client){
   int sz = ArraySize(glbServers);
   if(sz > 0) {
      
      string message = "SERVERS_";
      
      for(int i=0; i<sz; i++) {
         StringAdd(message, IntegerToString(glbServers[i]));
         if(i < sz-1) {
            StringAdd(message, ",");
         }
      }
      
      StringAdd(message, CRLF);    
      
      client.socket.Send(message);
   }   
}


/**
 * Envia a lista com as portas dos servidores para todos os clientes
 */
void sendServersToAll(){
   for (int i = ArraySize(glbClients) - 1; i >= 0; i--) {
      sendServers(glbClients[i]);
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
   string command;
   int k;
   do {
      line = client.socket.Receive(CRLF);               
      k = StringSplit(line, StringGetCharacter("_", 0), parts); 
      if(k > 0) {
         command = parts[0];
         
         if (command == "quote") {
            client.socket.Send(Symbol() + "," + DoubleToString(SymbolInfoDouble(Symbol(), SYMBOL_BID), 6) + "," + DoubleToString(SymbolInfoDouble(Symbol(), SYMBOL_ASK), 6) + "\r\n");
   
         }
         else if (command == "CLOSE") {            
            bForceClose = true;
            
         } 
         else if (command == "I_AM_EA") {
            // Esse cliente é um EA
            int port = StringToInteger(parts[1]);
            client.type = EA;
            client.port = port;
            
            // Informa a todos os interessados sobre as portas dos EA
            sendServersToAll();
         } 
         else if (command != "") {
            // Potentially handle other commands etc here.
            // For example purposes, we'll simply print messages to the Experts log
            Print("<- ", command);
         }
      }      
   } while (line != "");

   // If the socket has been closed, or the client has sent a close message,
   // release the socket and shuffle the glbClients[] array
   if (!client.socket.IsSocketConnected() || bForceClose) {
      Print("JTrade: Client has disconnected");

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
 * Permite receber mensagens do Master.
 *
 * O master apenas envia a lista as portas dos outros clients. 
 *
 * Essa lista é usada para que, se o master desligar, um dos clientes vai assumir a posição de master.
 */
void handleIncomingFromMaster() {
   
   if(glbClientSocket == NULL){
      // Não está conectado
      return;
   }   

   string line;
   string parts[];
   string command;
   int k;
   do {
      line = glbClientSocket.Receive(CRLF);               
      k = StringSplit(line, StringGetCharacter("_", 0), parts); 
      if(k > 0) {
         command = parts[0];
         
         // Listagem das portas de todos os outros servers (EA's) abertos 
         if (command == "SERVERS") {
            
            string portsStr[];
            int l = StringSplit(parts[1], StringGetCharacter(",", 0), portsStr);
            
            // Redimensiona a lista de servers
            ArrayResize(glbServers, l);
            
            int serverPort = ServerPort;
            if(l > 0) {
               for(int i=0; i<l; i++) {
                  glbServers[i] = StringToInteger(portsStr[i]);
                  serverPort = MathMax(serverPort, glbServers[i]);
               }
            }
            
            // Inicializa o server, se necessário e salva o número da porta 
            if(!glbServerSocket) {
               while(!createServer(++serverPort)){
                  continue;
               }
            }
         }
      }
   } while (line != "");

   // A conexão com o Master foi perdida
   if (!glbClientSocket.IsSocketConnected()) {
      Print("JTrade: Client has disconnected");

      // Cliente está morto. Destroi o objeto
      delete glbClientSocket;
      
      // Verifica se o cliente atual pode tornar-se master
      
      // ou faz a conexão com o novo master
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
