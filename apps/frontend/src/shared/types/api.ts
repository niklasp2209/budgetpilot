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

export type MyOrganization = {
  id: string;
  name: string;
  slug: string;
  role: MembershipRole;
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
