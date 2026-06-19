export type OrganizationPermission =
  | "ORGANIZATION_READ"
  | "MEMBERS_MANAGE"
  | "INVITES_MANAGE"
  | "ACCOUNTING_READ"
  | "ACCOUNTING_WRITE"
  | "BUDGET_READ"
  | "BUDGET_WRITE"
  | "REPORTING_READ"
  | "PERMISSION_GROUPS_MANAGE";

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
};

export type ApiErrorBody = {
  code: string;
  message: string;
};

export type MeResponse = {
  id: string;
  email: string;
};

export type MembershipRole = "OWNER" | "ADMIN" | "MEMBER" | "VIEWER";

export type OrganizationMember = {
  userId: string;
  email: string;
  role: MembershipRole;
  status: string;
  permissionGroupIds: string[];
};

export type PermissionGroup = {
  id: string;
  name: string;
  permissions: OrganizationPermission[];
};

export const ASSIGNABLE_MEMBERSHIP_ROLES: MembershipRole[] = ["ADMIN", "MEMBER", "VIEWER"];

export const ASSIGNABLE_PERMISSIONS: OrganizationPermission[] = [
  "ORGANIZATION_READ",
  "MEMBERS_MANAGE",
  "INVITES_MANAGE",
  "ACCOUNTING_READ",
  "ACCOUNTING_WRITE",
  "BUDGET_READ",
  "BUDGET_WRITE",
  "REPORTING_READ",
  "PERMISSION_GROUPS_MANAGE"
];

export type MyOrganization = {
  id: string;
  name: string;
  slug: string;
  currency: string;
  role: MembershipRole;
  permissions: OrganizationPermission[];
};

export type Account = {
  id: string;
  name: string;
  currency: string;
};

export type CategoryType = "INCOME" | "EXPENSE" | "TRANSFER";

export type Category = {
  id: string;
  name: string;
  type: CategoryType;
};

export type Transaction = {
  id: string;
  accountId: string;
  categoryId: string;
  amountCents: number;
  currency: string;
  bookedAt: string;
  description: string | null;
};

export type CashflowReport = {
  from: string;
  to: string;
  incomeCents: number;
  expenseCents: number;
  netCents: number;
};

export type CategoryAmount = {
  categoryId: string;
  categoryName: string;
  type: string;
  amountCents: number;
};

export type BudgetVsActualItem = {
  categoryId: string;
  categoryName: string;
  budgetCents: number;
  actualCents: number;
};

export type BudgetVsActualReport = {
  budgetId: string;
  items: BudgetVsActualItem[];
};

export type Budget = {
  id: string;
  name: string;
  periodStart: string;
  currency: string;
};

export type BudgetSummary = {
  budgetId: string;
  periodStart: string;
  totalBudgetCents: number;
  totalExpenseCents: number;
};

export type BudgetItem = {
  id: string;
  categoryId: string;
  categoryName: string;
  amountCents: number;
};
