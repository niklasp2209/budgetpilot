"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode
} from "react";
import { ApiError } from "@/shared/api/client";
import { createOrganization, fetchMyOrganizations } from "@/shared/api/organizations";
import {
  getStoredOrganizationId,
  setStoredOrganizationId
} from "@/shared/lib/storage";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import type { MyOrganization } from "@/shared/types/api";

type OrganizationContextValue = {
  organizations: MyOrganization[];
  selectedOrganization: MyOrganization | null;
  isLoading: boolean;
  error: string | null;
  selectOrganization: (organizationId: string) => void;
  createFirstOrganization: (name: string, slug: string, currency: string) => Promise<void>;
  reload: () => Promise<void>;
};

const OrganizationContext = createContext<OrganizationContextValue | null>(null);

type OrganizationProviderProps = Readonly<{
  children: ReactNode;
}>;

export function OrganizationProvider({ children }: OrganizationProviderProps) {
  const { t } = useTranslation();
  const [organizations, setOrganizations] = useState<MyOrganization[]>([]);
  const [selectedOrganizationId, setSelectedOrganizationId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadOrganizations = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const loaded = await fetchMyOrganizations();
      setOrganizations(loaded);

      const storedId = getStoredOrganizationId();
      const storedStillValid = storedId
        ? loaded.some((organization) => organization.id === storedId)
        : false;

      if (storedStillValid && storedId) {
        setSelectedOrganizationId(storedId);
      } else if (loaded.length > 0) {
        setSelectedOrganizationId(loaded[0].id);
        setStoredOrganizationId(loaded[0].id);
      } else {
        setSelectedOrganizationId(null);
      }
    } catch (caught) {
      if (caught instanceof ApiError) {
        setError(caught.message);
      } else {
        setError(t("org.loadFailed"));
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;

    async function bootstrapOrganizations() {
      await loadOrganizations();
      if (!active) {
        return;
      }
    }

    void bootstrapOrganizations();

    return () => {
      active = false;
    };
  }, [loadOrganizations]);

  const selectOrganization = useCallback((organizationId: string) => {
    setSelectedOrganizationId(organizationId);
    setStoredOrganizationId(organizationId);
  }, []);

  const createFirstOrganization = useCallback(async (name: string, slug: string, currency: string) => {
    const created = await createOrganization(name, slug, currency);
    setOrganizations((current) => [...current, created]);
    setSelectedOrganizationId(created.id);
    setStoredOrganizationId(created.id);
  }, []);

  const selectedOrganization = useMemo(
    () => organizations.find((organization) => organization.id === selectedOrganizationId) ?? null,
    [organizations, selectedOrganizationId]
  );

  const value = useMemo(
    () => ({
      organizations,
      selectedOrganization,
      isLoading,
      error,
      selectOrganization,
      createFirstOrganization,
      reload: loadOrganizations
    }),
    [
      organizations,
      selectedOrganization,
      isLoading,
      error,
      selectOrganization,
      createFirstOrganization,
      loadOrganizations
    ]
  );

  return <OrganizationContext.Provider value={value}>{children}</OrganizationContext.Provider>;
}

export function useOrganization(): OrganizationContextValue {
  const context = useContext(OrganizationContext);
  if (!context) {
    throw new Error("useOrganization must be used within OrganizationProvider.");
  }
  return context;
}
