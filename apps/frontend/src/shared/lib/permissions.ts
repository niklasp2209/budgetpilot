import type { MyOrganization, OrganizationPermission } from "@/shared/types/api";

export function hasPermission(
  organization: MyOrganization | null,
  permission: OrganizationPermission
): boolean {
  if (!organization) {
    return false;
  }
  return organization.permissions.includes(permission);
}
