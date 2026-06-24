class User {
  final int id;
  final String email;
  final String role;
  final String? govIdType;
  final String? govIdNumberMasked;
  final int workspaceId;

  User({
    required this.id,
    required this.email,
    required this.role,
    this.govIdType,
    this.govIdNumberMasked,
    required this.workspaceId,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int,
      email: json['email'] as String,
      role: json['role'] as String,
      govIdType: json['govIdType'] as String?,
      govIdNumberMasked: json['govIdNumberMasked'] as String?,
      workspaceId: json['workspaceId'] as int,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'role': role,
      'govIdType': govIdType,
      'govIdNumberMasked': govIdNumberMasked,
      'workspaceId': workspaceId,
    };
  }
}
