import { StatementItemPayload } from "@/app/types/api";

export const mapStatementsToPayload = (
  statements: any[]
): StatementItemPayload[] => {
  return statements.map((item) => ({
    id: item.id ?? '',
    date: item.timestamp ?? item.createdOn ?? '',
    description: item.description ?? '',
    type: item.type ?? '',
    currencyType: item.currencyType ?? '',
    amount: Number(item.amount ?? 0),
    balance: Number(item.availableBalance ?? item.previousBalance ?? 0),
    reference: item.referenceNo ?? item.transactionId ?? '',
  }));
};