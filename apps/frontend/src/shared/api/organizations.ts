import { DEFAULT_ORGANIZATION_CURRENCY } from "@/shared/lib/currencies";
import { apiRequest } from "@/shared/api/client";
import type { MeResponse, MyOrganization } from "@/shared/types/api";

export function fetchMe(): Promise<MeResponse> {
  return apiRequest<MeResponse>("/api/v1/me");
}

export function fetchMyOrganizations(): Promise<MyOrganization[]> {
  return apiRequest<MyOrganization[]>("/api/v1/me/organizations");
}

export function createOrganization(
  name: string,
  slug: string,
  currency: string = DEFAULT_ORGANIZATION_CURRENCY
): Promise<MyOrganization> {
  return apiRequest<{ id: string; name: string; slug: string; currency: string }>("/api/v1/organizations", {
    method: "POST",
    body: { name, slug, currency }
  }).then(async () => {
    const organizations = await fetchMyOrganizations();
    const created = organizations.find((organization) => organization.slug === slug);
    if (!created) {
      throw new Error("Organization was created but could not be loaded.");
    }
    return created;
  });
}

export function updateOrganization(
  organizationId: string,
  name: string,
  slug: string,
  currency: string
): Promise<{ id: string; name: string; slug: string; currency: string }> {
  return apiRequest<{ id: string; name: string; slug: string; currency: string }>(
    `/api/v1/organizations/${organizationId}`,
    {
      method: "PATCH",
      body: { name, slug, currency }
    }
  );
}

export function deleteOrganization(organizationId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/organizations/${organizationId}`, {
    method: "DELETE"
  });
}

export function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  return apiRequest<void>("/api/v1/me/password", {
    method: "PUT",
    body: { currentPassword, newPassword }
  });
}
