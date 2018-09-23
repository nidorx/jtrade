#property strict

//+------------------------------------------------------------------+
//|                                                Lib CisNewBar.mqh |
//|                                                Lib CisNewBar.mqh |
//|                                            Copyright 2010, Lizar |
//|                                               Lizar-2010@mail.ru |
//|                                              Revision 2010.09.27 |
//|                             https://www.mql5.com/en/articles/159 |
//+------------------------------------------------------------------+

/**
 * Permite detectar novas barras para o periodo informado
 */
class NewBarDetector {
   protected:
      datetime          m_lastbar_time;   // Time of opening last bar
      ENUM_TIMEFRAMES   m_period;         // Chart period
      uint              m_retcode;        // Result code of detecting new bar 
      int               m_new_bars;       // Number of new bars
      
   public:
      void              NewBarDetector( ENUM_TIMEFRAMES period );     
      int               countNewBar();
      bool              isNewBar(datetime new_Time);
      uint              getRetCode() const { return m_retcode; }
      datetime          getLastBarTime() const { return m_lastbar_time; }
      int               getNewBars() const { return m_new_bars; }
};

void NewBarDetector::NewBarDetector( ENUM_TIMEFRAMES period ) {
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
int NewBarDetector::countNewBar() {

   //int period_seconds = PeriodSeconds(m_period);                           // Number of seconds in current chart period
   //datetime new_time = TimeCurrent() / period_seconds * period_seconds;    // Time of bar opening on current chart
   //if(isNewBar(new_time)) 

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
bool NewBarDetector::isNewBar(datetime newbar_time) {
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
