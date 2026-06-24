import 'user.dart';
import 'room.dart';

class Lease {
  final int id;
  final User tenant;
  final Room room;
  final String leaseType; // MONTHLY, FIXED_PERIOD
  final String startDate;
  final String? endDate;
  final double rentAmount;
  final String status; // ACTIVE, TERMINATED, COMPLETED
  final int workspaceId;

  Lease({
    required this.id,
    required this.tenant,
    required this.room,
    required this.leaseType,
    required this.startDate,
    this.endDate,
    required this.rentAmount,
    required this.status,
    required this.workspaceId,
  });

  factory Lease.fromJson(Map<String, dynamic> json) {
    return Lease(
      id: json['id'] as int,
      tenant: User.fromJson(json['tenant'] as Map<String, dynamic>),
      room: Room.fromJson(json['room'] as Map<String, dynamic>),
      leaseType: json['leaseType'] as String,
      startDate: json['startDate'] as String,
      endDate: json['endDate'] as String?,
      rentAmount: (json['rentAmount'] as num).toDouble(),
      status: json['status'] as String,
      workspaceId: json['workspaceId'] as int,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'tenant': tenant.toJson(),
      'room': room.toJson(),
      'leaseType': leaseType,
      'startDate': startDate,
      'endDate': endDate,
      'rentAmount': rentAmount,
      'status': status,
      'workspaceId': workspaceId,
    };
  }
}
