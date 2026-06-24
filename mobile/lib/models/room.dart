class Room {
  final int id;
  final String roomNumber;
  final int totalBeds;
  final int vacantBeds;
  final double pricePerMonth;
  final int workspaceId;

  Room({
    required this.id,
    required this.roomNumber,
    required this.totalBeds,
    required this.vacantBeds,
    required this.pricePerMonth,
    required this.workspaceId,
  });

  factory Room.fromJson(Map<String, dynamic> json) {
    return Room(
      id: json['id'] as int,
      roomNumber: json['roomNumber'] as String,
      totalBeds: json['totalBeds'] as int,
      vacantBeds: json['vacantBeds'] as int,
      pricePerMonth: (json['pricePerMonth'] as num).toDouble(),
      workspaceId: json['workspaceId'] as int,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'roomNumber': roomNumber,
      'totalBeds': totalBeds,
      'vacantBeds': vacantBeds,
      'pricePerMonth': pricePerMonth,
      'workspaceId': workspaceId,
    };
  }
}
