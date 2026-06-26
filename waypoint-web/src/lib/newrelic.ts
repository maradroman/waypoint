import { BrowserAgent } from '@newrelic/browser-agent/loaders/browser-agent'

const accountId = import.meta.env.VITE_NEW_RELIC_ACCOUNT_ID
const agentId = import.meta.env.VITE_NEW_RELIC_APP_ID
const licenseKey = import.meta.env.VITE_NEW_RELIC_LICENSE_KEY

export function initNewRelic() {
  if (!accountId || !agentId || !licenseKey) return

  new BrowserAgent({
    info: {
      beacon: 'bam.nr-data.net',
      errorBeacon: 'bam.nr-data.net',
      licenseKey,
      applicationID: agentId,
      ...({ accountID: accountId, trustKey: accountId, agentID: agentId }),
    },
  })
}
