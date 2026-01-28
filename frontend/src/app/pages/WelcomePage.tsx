import { useState } from "react"; // Añade useRef si quieres controlar mejor el intervalo
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { FileUploader } from "../components/FileUploader";
import { DocuMindService } from "../service/DocuMindService"; 
import { toast } from "sonner";

export function WelcomePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  
  const [loadingMessage, setLoadingMessage] = useState(t("welcome.file.initialStatus"));

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsLoading(true);
    setLoadingMessage(t("welcome.file.initialStatus")); 

    try {
      // 1. Upload file and get Job ID
      const { jobId } = await DocuMindService.uploadDocument(selectedFile);
      
      setLoadingMessage(t("welcome.file.processing"));

      // 2. Start Polling (Check every 2 seconds)
      const pollInterval = setInterval(async () => {
        try {
          const status = await DocuMindService.getJobStatus(jobId);
          console.log(`Job status ${jobId}: ${status}`);

          if (status === "COMPLETED") {
            clearInterval(pollInterval);
            toast.success(t("welcome.file.success"));
            navigate("/chat");
            
          } else if (status.startsWith("ERROR") || status === "NOT_FOUND") {
            // Error in backend during processing
            clearInterval(pollInterval);
            toast.error(t("welcome.file.uploadError"), { 
              description: status 
            });
            setIsLoading(false);
          } 

        } catch (pollError) {
          // Error during polling
          clearInterval(pollInterval);
          console.error(pollError);
          toast.error(t("welcome.file.uploadErrorDescription"));
          setIsLoading(false);
        }
      }, 2000);
    } catch (error: any) {
      console.error(error);
      let errorMessage = t("welcome.file.uploadErrorDescription");
      
      toast.error(t("welcome.file.uploadError"), {
        description: errorMessage,
        duration: 5000, 
      });
      setIsLoading(false);
    }
  };

  return (
    <main className="welcome-page-container">
      <h1 className="welcome-title">{t("welcome.title")}</h1>
      <p className="welcome-description">{t("welcome.description")}</p>

      {/* Upload Component */}
      <FileUploader 
        onFileSelect={(file) => setSelectedFile(file)} 
        selectedFileName={selectedFile?.name || null}
        disabled={isLoading}
      />

      {/* Confirmation Button with Loading State */}
      <button
        className="btn-primary flex items-center justify-center gap-2"
        disabled={!selectedFile || isLoading}
        onClick={handleUpload}
      >
        {isLoading ? (
          <>
            <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>{loadingMessage}</span>
          </>
        ) : (
          t("welcome.confirmationButton")
        )}
      </button>
    </main>
  );
}