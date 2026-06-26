export const formatAmount = (amount: number | string | undefined | null): string => {
  if (amount === null || amount === undefined) return "0.00";
  const amountStr = amount.toString().replace(/,/g, '');
  const number = parseFloat(amountStr);

  if (isNaN(number)) return "0.00";
  return number.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};