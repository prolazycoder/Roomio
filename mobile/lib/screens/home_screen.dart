import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../models/room.dart';
import '../models/lease.dart';
import '../config/api_config.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  List<Room> _rooms = [];
  List<Lease> _leases = [];
  bool _isFetchingData = false;

  @override
  void initState() {
    super.initState();
    _fetchDashboardData();
  }

  Future<void> _fetchDashboardData() async {
    setState(() {
      _isFetchingData = true;
    });

    final authProvider = Provider.of<AuthProvider>(context, listen: false);
    final apiService = authProvider.apiService;

    try {
      // 1. Fetch Rooms
      final roomsResponse = await apiService.dio.get(ApiConfig.rooms);
      if (roomsResponse.statusCode == 200) {
        final List<dynamic> roomsJson = roomsResponse.data as List;
        _rooms = roomsJson.map((e) => Room.fromJson(e as Map<String, dynamic>)).toList();
      }

      // 2. Fetch Active Leases
      final leasesResponse = await apiService.dio.get('${ApiConfig.leases}/active');
      if (leasesResponse.statusCode == 200) {
        final List<dynamic> leasesJson = leasesResponse.data as List;
        _leases = leasesJson.map((e) => Lease.fromJson(e as Map<String, dynamic>)).toList();
      }
    } catch (e) {
      // API call failed
    } finally {
      if (mounted) {
        setState(() {
          _isFetchingData = false;
        });
      }
    }
  }

  void _bookBed(int roomId) async {
    final authProvider = Provider.of<AuthProvider>(context, listen: false);
    final apiService = authProvider.apiService;

    try {
      final response = await apiService.dio.post('${ApiConfig.rooms}/$roomId/book-atomic');
      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Bed booked successfully (Atomic Update)!')),
        );
        _fetchDashboardData(); // Refresh counts
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Failed to book bed: No vacancies available!')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = Provider.of<AuthProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard'),
        backgroundColor: Colors.blueAccent,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _fetchDashboardData,
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => authProvider.logout(),
          ),
        ],
      ),
      body: _isFetchingData
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _fetchDashboardData,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Welcome, ${authProvider.currentUser?.email ?? "User"}!',
                      style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Workspace: Tenant #${authProvider.workspaceId}',
                      style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                    ),
                    const SizedBox(height: 24),
                    
                    // --- Rooms Header ---
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Inventory Rooms',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        Text(
                          '${_rooms.length} found',
                          style: const TextStyle(color: Colors.blueAccent),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    _rooms.isEmpty
                        ? const Card(
                            child: Padding(
                              padding: EdgeInsets.all(16.0),
                              child: Text('No rooms registered in this workspace.'),
                            ),
                          )
                        : ListView.builder(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            itemCount: _rooms.length,
                            itemBuilder: (context, index) {
                              final room = _rooms[index];
                              return Card(
                                child: ListTile(
                                  title: Text('Room ${room.roomNumber}'),
                                  subtitle: Text(
                                    'Vacant beds: ${room.vacantBeds}/${room.totalBeds} • \$${room.pricePerMonth}/mo',
                                  ),
                                  trailing: ElevatedButton(
                                    onPressed: room.vacantBeds > 0
                                        ? () => _bookBed(room.id)
                                        : null,
                                    child: const Text('BOOK BED'),
                                  ),
                                ),
                              );
                            },
                          ),
                    const SizedBox(height: 24),

                    // --- Active Leases Header ---
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Active Leases',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        Text(
                          '${_leases.length} active',
                          style: const TextStyle(color: Colors.blueAccent),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    _leases.isEmpty
                        ? const Card(
                            child: Padding(
                              padding: EdgeInsets.all(16.0),
                              child: Text('No active leases in this workspace.'),
                            ),
                          )
                        : ListView.builder(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            itemCount: _leases.length,
                            itemBuilder: (context, index) {
                              final lease = _leases[index];
                              return Card(
                                child: ListTile(
                                  leading: const Icon(Icons.assignment),
                                  title: Text(lease.tenant.email),
                                  subtitle: Text(
                                    'Room ${lease.room.roomNumber} • ${lease.leaseType} • \$${lease.rentAmount}/mo',
                                  ),
                                  trailing: const Icon(
                                    Icons.check_circle,
                                    color: Colors.green,
                                  ),
                                ),
                              );
                            },
                          ),
                  ],
                ),
              ),
            ),
    );
  }
}
