import i18next from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import en from "./locales/en.json";
import es from "./locales/es.json";

i18next
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: "es",
    resources: {
      en: { translation: en },
      es: { translation: es },
    },
    interpolation: {
      escapeValue: false,
    },
    detection: {
  order: ["navigator", "localStorage", "htmlTag"],
  caches: ["localStorage"],
},
    // Lista de idiomas soportados
    supportedLngs: ["en", "es"],
    nonExplicitSupportedLngs: false,
  });

export default i18next;