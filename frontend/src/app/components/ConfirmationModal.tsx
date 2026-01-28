import type { ConfirmationModalProps } from "../types/ConfirmationModalProps";
import { useTranslation } from "react-i18next";

export function ConfirmationModal({ isOpen, onClose, onConfirm}: ConfirmationModalProps) {
  if (!isOpen) return null;
  const { t } = useTranslation();
  return (
    <div className="modal-overlay">
      <div className="modal-container">
        
        <h3 className="modal-title">
          {t("welcome.modal.title")}
        </h3>
        
        <div className="mt-2">
          <p className="modal-description">
            {t("welcome.modal.description")}
          </p>
        </div>

        <div className="modal-actions">
          <button
            type="button"
            className="btn-secondary"
            onClick={onClose}
          >
            {t("welcome.modal.cancelButton")}
          </button>
          
          <button
            type="button"
            className="btn-danger"
            onClick={onConfirm}
          >
            {t("welcome.modal.confirmButton")}
          </button>
        </div>
        
      </div>
    </div>
  );
}