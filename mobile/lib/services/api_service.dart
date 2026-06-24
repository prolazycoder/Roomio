import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';

class ApiService {
  final Dio dio = Dio(BaseOptions(baseUrl: ApiConfig.baseUrl));
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  ApiService() {
    _initializeInterceptors();
  }

  void _initializeInterceptors() {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (RequestOptions options, RequestInterceptorHandler handler) async {
          // 1. Inject Access Token if available in secure storage
          final accessToken = await _storage.read(key: 'access_token');
          if (accessToken != null) {
            options.headers['Authorization'] = 'Bearer $accessToken';
          }

          // 2. Inject Workspace ID if available in secure storage
          final workspaceId = await _storage.read(key: 'workspace_id');
          if (workspaceId != null) {
            options.headers['X-Workspace-Id'] = workspaceId;
          }

          return handler.next(options);
        },
        onError: (DioException error, ErrorInterceptorHandler handler) async {
          // 3. Catch 401 Unauthorized error and execute transparent token refresh rotation
          if (error.response?.statusCode == 401) {
            final refreshToken = await _storage.read(key: 'refresh_token');

            if (refreshToken == null) {
              // No session refresh token available
              return handler.next(error);
            }

            try {
              // Create a clean client instance for refresh request to bypass standard interceptor logic
              final refreshDio = Dio(BaseOptions(baseUrl: ApiConfig.baseUrl));
              
              final response = await refreshDio.post(
                ApiConfig.authRefresh,
                data: {'refreshToken': refreshToken},
              );

              if (response.statusCode == 200) {
                final newAccessToken = response.data['accessToken'] as String;
                final newRefreshToken = response.data['refreshToken'] as String;

                // Save newly rotated credentials to keychain/keystore secure storage
                await _storage.write(key: 'access_token', value: newAccessToken);
                await _storage.write(key: 'refresh_token', value: newRefreshToken);

                // Retry original request with the new access token
                final options = error.requestOptions;
                options.headers['Authorization'] = 'Bearer $newAccessToken';
                
                final clonedResponse = await dio.request(
                  options.path,
                  options: Options(
                    method: options.method,
                    headers: options.headers,
                  ),
                  data: options.data,
                  queryParameters: options.queryParameters,
                );

                return handler.resolve(clonedResponse);
              }
            } catch (refreshError) {
              // Refresh Token expired or revoked. Wipe storage.
              await _storage.delete(key: 'access_token');
              await _storage.delete(key: 'refresh_token');
              await _storage.delete(key: 'workspace_id');
              return handler.next(error);
            }
          }

          return handler.next(error);
        },
      ),
    );
  }
}
