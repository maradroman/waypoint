export function formatMoney(cents: number): string {
  const dollars = cents / 100
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(dollars)
}
