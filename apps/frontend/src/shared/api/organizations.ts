import { apiRequest } from "@/shared/api/client";
import type { MeResponse, MyOrganization } from "@/shared/types/api";

export function fetchMe(): Promise<MeResponse> {
  return apiRequest<MeResponse>("/api/v1/me");
}

export function fetchMyOrganizations(): Promise<MyOrganization[]> {
  return apiRequest<MyOrganization[]>("/api/v1/me/organizations");
}

export function createOrganization(name: string, slug: string): Promise<MyOrganization> {
  return apiRequest<{ id: string; name: string; slug: string }>("/api/v1/organizations", {
    method: "POST",
    body: { name, slug }
  }).then(async () => {
    const organizations = await fetchMyOrganizations();
    const created = organizations.find((organization) => organization.slug === slug);
    if (!created) {
      throw new Error("Organization was created but could not be loaded.");
    }
    return created;
  });
}
