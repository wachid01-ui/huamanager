package com.huamanager.modem.router.app.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Interface defining all localized strings used across HuaManager Android
 */
interface AppStrings {
    // Navigation items
    val navSignal: String
    val navTower: String
    val navBandLock: String
    val navSms: String
    val navTools: String

    // General & Status
    val connected: String
    val disconnected: String
    val autoRefreshActive: String
    val autoRefreshPaused: String
    val processing: String
    val success: String
    val failed: String
    val save: String
    val close: String
    val cancel: String
    val confirm: String
    val refresh: String
    val copy: String
    val copied: String

    // Signal Quality Labels
    val qualityExcellent: String
    val qualityGood: String
    val qualityFair: String
    val qualityPoor: String
    val qualitySearching: String

    // Signal Metrics Labels & Descriptions
    val noiseGood: String
    val noiseFair: String
    val noisePoor: String
    val signalGood: String
    val signalFair: String
    val signalPoor: String

    // Dashboard
    val realtimeSignalParams: String
    val notConnectedMsg: String
    val signalMetricRSRP: String
    val signalMetricSINR: String
    val signalMetricRSRQ: String
    val signalMetricRSSI: String
    val signalMetricPCI: String
    val signalMetricCellId: String
    val signalMetricBand: String
    val signalMetricFreq: String
    val signalMetricDlUlFreq: String
    val signalMetricBandwidth: String
    val signalMetricNetworkType: String
    val signalMetricOperator: String
    val signalMetricRoaming: String
    val roamingActive: String
    val roamingInactive: String

    // Tower & Antenna
    val towerInfoTitle: String
    val towerSubtitle: String
    val enodebSectorTitle: String
    val enodebIdLabel: String
    val sectorAntennaLabel: String
    val physicalSectorLabel: String
    val localCellIdLabel: String
    val physicalSectorDesc: String
    val antennaAzimuthTitle: String
    val antennaAzimuthSubtitle: String
    val estimatedTowerDirTitle: String
    val estimatedTowerDirSubtitle: String
    val towerAdviceHeader: String
    val towerAdviceIntro: String
    val towerAdvicePointing: String
    val towerAdviceNote: String
    val openCellMapperBtn: String
    val manualCellIdCalcTitle: String
    val manualCellIdCalcSubtitle: String
    val manualCellIdInputLabel: String
    val calculateBtn: String
    val calcResultTitle: String
    val calcResultEnodeB: String
    val calcResultSector: String
    val calcResultAzimuth: String
    val calcResultTowerDir: String
    val invalidCellIdError: String
    val directionNorth: String
    val directionEast: String
    val directionSouth: String
    val directionWest: String
    val directionNorthEast: String
    val directionSouthEast: String
    val directionSouthWest: String
    val directionNorthWest: String
    val sectorWord: String
    val frequencyLabel: String
    val bearingSouthOrSouthwest: String
    val bearingNorthOrNorthwest: String
    val bearingEastOrSoutheast: String
    val bearingSouth: String
    val sectorUnknown: String
    fun getEstimatedTowerBearing(physicalSector: Int): String
    fun getSectorAzimuthDescription(physicalSector: Int, lcid: Int?): String
    fun sectorCarrierNote(lcid: Int, physicalSector: Int): String
    fun getAntennaPointingAdvice(bearing: String, physicalSector: Int, lcidText: String): String
    val towerCopyHeader: String
    val towerCopyTowerLoc: String
    val towerCopyAimDir: String
    val towerCopyPosPrefix: String
    val towerCopyAimPrefix: String

    // Band Lock
    val bandLockTitle: String
    val bandLockSubtitle: String
    val activeBandLabel: String
    val selectBandsToLock: String
    val lockSelectedBandsBtn: String
    val resetAllBandsBtn: String
    val bandLockAuthRequired: String
    val bandLockSuccessMsg: String
    val bandLockFailedMsg: String
    val selectAtLeastOneBand: String
    val presetBandsTitle: String
    val presetBand3Title: String
    val presetBand3Subtitle: String
    val presetBand40Title: String
    val presetBand40Subtitle: String
    val presetBand1Title: String
    val presetBand1Subtitle: String
    val presetBand8Title: String
    val presetBand8Subtitle: String
    val presetAutoTitle: String
    val presetAutoSubtitle: String
    val carrierAggregationTitle: String
    val manualBandSelectionTitle: String
    val lockBtn: String
    val customCombinationLabel: String
    val badgeRecommended: String
    val badgeDefault: String
    val badgeCoverage: String
    fun getBandFrequency(bandNumber: Int, defaultFreq: String): String
    fun getBandDescription(bandNumber: Int, defaultDesc: String): String

    // SMS
    val smsTitle: String
    val smsSubtitle: String
    val sendSmsCardTitle: String
    val sendSmsCardSubtitle: String
    val recipientPhoneLabel: String
    val messageContentLabel: String
    val sendSmsBtn: String
    val sendingSms: String
    val smsSentSuccess: String
    val smsSentFailed: String
    val inboxTitle: String
    val inboxSubtitle: String
    val noSmsMessages: String
    val deleteSmsBtn: String
    val deleteSmsSuccess: String
    val deleteSmsFailed: String

    // Tools & Settings
    val connectionAuthTitle: String
    val gatewayIpLabel: String
    val usernameLabel: String
    val passwordLabel: String
    val showPasswordDesc: String
    val hidePasswordDesc: String
    val statusLoggedIn: String
    val statusNotLoggedIn: String
    val loginBtn: String
    val logoutBtn: String
    val reloginBtn: String
    val loginSuccess: String
    val logoutSuccess: String
    val loginFailed: String
    val tipsTitle: String
    val tip1: String
    val tip2: String
    val tip3: String
    val tip4: String
    val tip5: String
    val modemControlsTitle: String
    val rebootBtn: String
    val mobileDataBtn: String
    val mobileDataOn: String
    val mobileDataOff: String
    val rebootConfirmTitle: String
    val rebootConfirmMsg: String
    val rebootProgressMsg: String
    val rebootFailedMsg: String
    val ussdTitle: String
    val ussdCodeLabel: String
    val ussdCallBtn: String
    val ussdCalling: String
    val ussdResultTitle: String
    val deviceInfoTitle: String
    val deviceInfoModel: String
    val deviceInfoFirmware: String
    val deviceInfoWebUi: String
    val deviceInfoHardware: String
    val deviceInfoImei: String
    val deviceInfoImsi: String
    val deviceInfoMac: String

    // Theme & Appearance
    val themeSectionTitle: String
    val themeSectionSubtitle: String
    val activeThemePrefix: String

    // Language Section
    val languageSectionTitle: String
    val languageSectionSubtitle: String
    val activeLanguagePrefix: String

    // Monetization & PRO Tier
    val proTitle: String
    val proPrice: String
    val proSubtitle: String
    val proUpgradeBtn: String
    val proActiveBadge: String
    val proFreeBadge: String
    val proRestoreBtn: String
    val proBenefit1: String
    val proBenefit2: String
    val proBenefit3: String
    val rewardUnlockTitle: String
    val rewardUnlockDesc: String
    val watchVideoBtn: String
    val adNotReady: String
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> {
    IndonesianStrings
}
