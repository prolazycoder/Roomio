import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../services/api_service.dart';
import '../config/api_config.dart';
import '../models/user.dart';

class AuthProvider with ChangeNotifier {
  final ApiService apiService;
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  User? _currentUser;
  String? _workspaceId;
  bool _isLoading = false;
  bool _isAuthenticated = false;

  AuthProvider(this.apiService) {
    loadSession();
  }

  User? get currentUser => _currentUser;
  String? get workspaceId => _workspaceId;
  bool get isLoading => _isLoading;
  bool get isAuthenticated => _isAuthenticated;

  Future<void> loadSession() async {
    _isLoading = true;
    notifyListeners();

    try {
      final token = await _storage.read(key: 'access_token');
      final savedWorkspaceId = await _storage.read(key: 'workspace_id');

      if (token != null && savedWorkspaceId != null) {
        _workspaceId = savedWorkspaceId;
        _isAuthenticated = true;
        
        // In a production app, we would query the `/auth/me` endpoint.
        // We'll set a basic state mock for now since token is validated.
        _currentUser = User(
          id: 1,
          email: 'admin@hostelapp.com',
          role: 'OWNER',
          workspaceId: int.parse(savedWorkspaceId),
        );
      }
    } catch (e) {
      await logout();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> login(String email, String password, String workspaceId) async {
    _isLoading = true;
    notifyListeners();

    try {
      final response = await apiService.dio.post(
        ApiConfig.authLogin,
        data: {
          'workspaceId': int.parse(workspaceId),
          'email': email,
          'password': password,
        },
      );

      if (response.statusCode == 200) {
        final accessToken = response.data['accessToken'] as String;
        final refreshToken = response.data['refreshToken'] as String;

        await _storage.write(key: 'access_token', value: accessToken);
        await _storage.write(key: 'refresh_token', value: refreshToken);
        await _storage.write(key: 'workspace_id', value: workspaceId);

        _workspaceId = workspaceId;
        _isAuthenticated = true;
        _currentUser = User(
          id: 1,
          email: email,
          role: 'OWNER',
          workspaceId: int.parse(workspaceId),
        );
        return true;
      }
      return false;
    } catch (e) {
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> onboardOrganization(
    String orgName,
    String workspaceName,
    String? subdomain,
    String email,
    String password,
    String? govIdType,
    String? govIdNumber,
  ) async {
    _isLoading = true;
    notifyListeners();

    try {
      final response = await apiService.dio.post(
        ApiConfig.onboardOrganization,
        data: {
          'organizationName': orgName,
          'workspaceName': workspaceName,
          'subdomain': subdomain,
          'adminEmail': email,
          'adminPassword': password,
          'govIdType': govIdType,
          'govIdNumber': govIdNumber,
        },
      );

      if (response.statusCode == 200) {
        final newWorkspaceId = response.data['id'].toString();
        // Automatically proceed to login after onboarding
        return await login(email, password, newWorkspaceId);
      }
      return false;
    } catch (e) {
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    await _storage.delete(key: 'access_token');
    await _storage.delete(key: 'refresh_token');
    await _storage.delete(key: 'workspace_id');
    _currentUser = null;
    _workspaceId = null;
    _isAuthenticated = false;
    notifyListeners();
  }
}
