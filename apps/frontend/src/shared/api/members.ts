import { apiRequest } from "@/shared/api/client";
import type { MembershipRole, OrganizationMember } from "@/shared/types/api";

export function fetchMembers(organizationId: string): Promise<OrganizationMember[]> {
  return apiRequest<OrganizationMember[]>(`/api/v1/organizations/${organizationId}/members`);
}

export function addMember(
  organizationId: string,
  payload: { email: string; password?: string; role: MembershipRole }
): Promise<OrganizationMember> {
  return apiRequest<OrganizationMember>(`/api/v1/organizations/${organizationId}/members`, {
    method: "POST",
    body: payload
  });
}

export function updateMemberRole(
  organizationId: string,
  userId: string,
  role: MembershipRole
): Promise<void> {
  return apiRequest<void>(`/api/v1/organizations/${organizationId}/members/${userId}/role`, {
    method: "PATCH",
    body: { role }
  });
}

export function removeMember(organizationId: string, userId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/organizations/${organizationId}/members/${userId}`, {
    method: "DELETE"
  });
}
