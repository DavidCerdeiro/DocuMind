import { useState, useRef, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { FileUploader } from "../components/FileUploader";
import { ProgressBar } from "../components/ProgressBar";
import { DocuMindService } from "../service/DocuMindService";

export function WelcomePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [progress, setProgress] = useState(0);

  const pollIntervalRef = useRef<number | null>(null);

  useEffect(() => {
    return () => stopPolling();
  }, []);

  const stopPolling = () => {
    if (pollIntervalRef.current !== null) {
      clearInterval(pollIntervalRef.current);
      pollIntervalRef.current = null;
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsLoading(true);
    setProgress(0);

    try {
      const { jobId } = await DocuMindService.uploadDocument(selectedFile);
      
      pollIntervalRef.current = window.setInterval(async () => {
        try {
          const jobData = await DocuMindService.getJobStatus(jobId);
          setProgress(jobData.progress);

          if (jobData.status === "COMPLETED") {
            stopPolling();
            toast.success(t("welcome.file.success"));
            setTimeout(() => navigate("/chat"), 500);
          } 
          else if (jobData.status === "ERROR" || jobData.status === "NOT_FOUND") {
            stopPolling();
            toast.error(t("welcome.file.uploadError"), { 
              description: jobData.message || "Unknown error"
            });
            setIsLoading(false);
          } 
        } catch (pollError) {
          stopPolling();
          console.error(pollError);
          toast.error(t("welcome.file.uploadErrorDescription"));
          setIsLoading(false);
        }
      }, 1000);

    } catch (error) {
      console.error(error);
      setIsLoading(false);
      toast.error(t("welcome.file.uploadError"));
    }
  };

  return (
    <main className="welcome-page-container">
      <h1 className="welcome-title">{t("welcome.title")}</h1>
      <p className="welcome-description">{t("welcome.description")}</p>

      <FileUploader 
        onFileSelect={(file) => {
            setSelectedFile(file);
            setProgress(0);
        }} 
        selectedFileName={selectedFile?.name || null}
        disabled={isLoading}
      />

      {isLoading && (
        <ProgressBar 
            progress={progress} 
            label={t("welcome.file.loading")} 
        />
      )}

      <button
        className={`btn-primary flex items-center justify-center gap-2 ${
            !isLoading ? 'btn-margin-idle' : 'btn-margin-loading'
        }`}
        disabled={!selectedFile || isLoading}
        onClick={handleUpload}
      >
        {isLoading ? (
          <>
            <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>{t("welcome.file.processing")}</span>
          </>
        ) : (
          t("welcome.confirmationButton")
        )}
      </button>
    </main>
  );
}