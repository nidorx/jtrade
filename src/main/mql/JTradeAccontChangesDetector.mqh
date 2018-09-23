#property strict

/**
 * Permite detectar alterações nos atributos da conta
 */
class AccontChangesDetector {
   protected:
      int       leverage;
      double    balance;
      double    equity;
      double    margin;
      double    margin_free;
      double    profit;
      datetime  lastcheck;

   public:
      void      AccontChangesDetector();  
      bool      hasChange();
};

void AccontChangesDetector::AccontChangesDetector() {
   leverage    = AccountInfoInteger(ACCOUNT_LEVERAGE);
   balance     = AccountInfoDouble(ACCOUNT_BALANCE);
   equity      = AccountInfoDouble(ACCOUNT_EQUITY);
   margin      = AccountInfoDouble(ACCOUNT_MARGIN);
   margin_free = AccountInfoDouble(ACCOUNT_MARGIN_FREE);
   profit      = AccountInfoDouble(ACCOUNT_PROFIT);
   lastcheck   = 0;
}

/**
 * Verifica se possui alguma mudança nas propriedades da conta
 */
bool AccontChangesDetector::hasChange() {

   if(lastcheck == TimeCurrent()){
      return false;
   }

   lastcheck = TimeCurrent();
   int n_leverage       = AccountInfoInteger(ACCOUNT_LEVERAGE);
   double n_balance     = AccountInfoDouble(ACCOUNT_BALANCE);
   double n_equity      = AccountInfoDouble(ACCOUNT_EQUITY);
   double n_margin      = AccountInfoDouble(ACCOUNT_MARGIN);
   double n_margin_free = AccountInfoDouble(ACCOUNT_MARGIN_FREE);
   double n_profit      = AccountInfoDouble(ACCOUNT_PROFIT);    

   bool changed = (
      leverage    != n_leverage    ||
      balance     != n_balance     ||
      equity      != n_equity      ||
      margin      != n_margin      ||
      margin_free != n_margin_free ||
      profit      != n_profit
   );

   if(changed){
      leverage    = n_leverage;
      balance     = n_balance;
      equity      = n_equity;
      margin      = n_margin;
      margin_free = n_margin_free;
      profit      = n_profit;

      return true;
   }

   return false;
}
