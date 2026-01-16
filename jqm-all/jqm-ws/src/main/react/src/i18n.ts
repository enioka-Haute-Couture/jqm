import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import enTranslation from "./locales/en/translation.json";
import frTranslation from "./locales/fr/translation.json";
import {
    frFR as frFRDatePickers,
    enUS as enUSDatePickers,
} from "@mui/x-date-pickers/locales";
import { fr } from "date-fns/locale/fr";
import { enUS } from "date-fns/locale/en-US";
import type { Locale } from "date-fns";

export interface LanguageConfig {
    name: string;
    dateFnsLocale: Locale;
    muiLocale: any;
}

export const languageConfig: Record<string, LanguageConfig> = {
    en: {
        name: "English",
        dateFnsLocale: enUS,
        muiLocale: enUSDatePickers,
    },
    fr: {
        name: "Français",
        dateFnsLocale: fr,
        muiLocale: frFRDatePickers,
    },
};

// Get language from localStorage or default to English
const i18nLanguage = localStorage.getItem("jqm-i18n-language") || "en";

i18n.use(initReactI18next).init({
    resources: {
        en: {
            translation: enTranslation,
        },
        fr: {
            translation: frTranslation,
        },
    },
    lng: i18nLanguage,
    fallbackLng: "en",
    interpolation: {
        escapeValue: false, // react already safes from xss
    },
});

// Save language to localStorage whenever it changes
i18n.on("languageChanged", (lng) => {
    localStorage.setItem("jqm-i18n-language", lng);
});

export default i18n;
