import { useTranslation } from "react-i18next";

interface ChatInputProps {
  inputValue: string;
  setInputValue: (val: string) => void;
  onSend: (e: React.FormEvent) => void;
  isLoading: boolean;
}

export function ChatInput({ inputValue, setInputValue, onSend, isLoading }: ChatInputProps) {
  const { t } = useTranslation();

  return (
    <footer className="chat-input-area">
      <div className="chat-container-limit">
        <form onSubmit={onSend} className="relative flex items-center gap-2">
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={isLoading ? "DocuMind está pensando..." : t("chat.placeholder")}
            disabled={isLoading}
            className="chat-input-field disabled:opacity-60 disabled:bg-slate-100"
            autoFocus
          />

          <button
            type="submit"
            disabled={!inputValue.trim() || isLoading}
            className="absolute right-2 p-2 bg-slate-900 text-white rounded-lg hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            aria-label={t("chat.send")}
          >
            {isLoading ? (
              <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            ) : (
              <svg className="w-5 h-5 transform rotate-90" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            )}
          </button>
        </form>
        <p className="text-xs text-center text-slate-400 mt-2">
          {t("chat.footerAdvice")}
        </p>
      </div>
    </footer>
  );
}