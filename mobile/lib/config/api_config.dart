class ApiConfig {
  // 10.0.2.2 is the special IP address mapping to localhost of the host machine from Android Emulator.
  // Use http://localhost:8080/api for iOS Simulator.
  static const String baseUrl = 'http://10.0.2.2:8080/api';
  
  static const String authRegister = '/auth/register';
  static const String authLogin = '/auth/login';
  static const String authRefresh = '/auth/refresh';
  
  static const String onboardOrganization = '/organizations/onboard';
  
  static const String rooms = '/rooms';
  static const String leases = '/leases';
  static const String financialInvoices = '/financial/invoices';
  static const String financialPayments = '/financial/payments';
}
