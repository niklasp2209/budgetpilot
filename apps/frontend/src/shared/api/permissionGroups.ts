import { apiRequest } from "@/shared/api/client";
import type { OrganizationPermission, PermissionGroup } from "@/shared/types/api";

export function fetchPermissionGroups(organizationId: string): Promise<PermissionGroup[]> {
  return apiRequest<PermissionGroup[]>(`/api/v1/organizations/${organizationId}/permission-groups`);
}

export function createPermissionGroup(
  organizationId: string,
  name: string,
  permissions: OrganizationPermission[]
): Promise<PermissionGroup> {
  return apiRequest<PermissionGroup>(`/api/v1/organizations/${organizationId}/permission-groups`, {
    method: "POST",
    body: { name, permissions }
  });
}

export function updatePermissionGroup(
  organizationId: string,
  groupId: string,
  name: string,
  permissions: OrganizationPermission[]
): Promise<PermissionGroup> {
  return apiRequest<PermissionGroup>(
    `/api/v1/organizations/${organizationId}/permission-groups/${groupId}`,
    {
      method: "PUT",
      body: { name, permissions }
    }
  );
}

export function deletePermissionGroup(organizationId: string, groupId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/organizations/${organizationId}/permission-groups/${groupId}`, {
    method: "DELETE"
  });
}

export function assignMemberPermissionGroups(
  organizationId: string,
  userId: string,
  groupIds: string[]
): Promise<void> {
  return apiRequest<void>(
    `/api/v1/organizations/${organizationId}/members/${userId}/permission-groups`,
    {
      method: "PUT",
      body: { groupIds }
    }
  );
}
