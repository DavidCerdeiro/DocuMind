import type { JobStatusResponse } from "../types/JobStatusResponse";

const DOCS_API_URL = "/api/docs";
const CHAT_API_URL = "/api/chat";

export const DocuMindService = {
  /**
   * Asynchronous upload of a document (PDF).
   * @param file The PDF file to upload.
   * @returns An object containing the jobId for tracking processing status.
   * @throws Error with specific messages for different failure cases.
   */
  uploadDocument: async (file: File): Promise<{ jobId: string }> => {
    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch(`${DOCS_API_URL}/upload`, {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        if (response.status === 504 || response.status === 502) throw new Error("Backend Unreachable");
        if (response.status === 415) throw new Error("Formato no soportado. Solo PDFs.");
        throw new Error(`Upload error: ${response.status} ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Error in uploadDocument:", error);
      throw error;
    }
  },

  /**
   * Gets the processing status of the uploaded document.
   * @param jobId The job ID returned from uploadDocument.
   * @returns A promise that resolves to the job status string.
   */
  getJobStatus: async (jobId: string): Promise<JobStatusResponse> => {
    try {
      const response = await fetch(`${DOCS_API_URL}/status/${jobId}`);
      
      // Si es 404, devolvemos un estado controlado para que el frontend no rompa
      if (response.status === 404) {
         return { status: "NOT_FOUND", progress: 0 };
      }

      if (!response.ok) {
        throw new Error("Error checking status");
      }

      return await response.json();
    } catch (error) {
      console.error("Error checking status:", error);
      throw error;
    }
  },

  /**
   * Sends a question to the AI model about the uploaded document.
   * @param question The text of the question.
   * @returns The AI's response as a string.
   * @throws Error "INFO_NOT_FOUND" if the AI does not have the answer in the document.
   */
  sendQuestion: async (question: string): Promise<string> => {
    try {
      const response = await fetch(CHAT_API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ question }),
      });

      if (response.status === 404) {
        throw new Error("INFO_NOT_FOUND");
      }

      if (!response.ok) {
         if (response.status === 504 || response.status === 502) {
            throw new Error("Backend Unreachable");
         }
         throw new Error(`Chat error: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      return data.answer;

    } catch (error) {
      console.error("Error in sendQuestion:", error);
      throw error;
    }
  },

  /**
   * Resets the current session by clearing the vector store.
   * @returns A promise that resolves when the reset is complete.
   * @throws Error with specific messages for different failure cases.
   */
  resetSession: async (): Promise<void> => {
    try {
      const response = await fetch(`${DOCS_API_URL}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        if (response.status === 504 || response.status === 502) {
            throw new Error("Backend Unreachable");
        }
        throw new Error(`Reset error: ${response.status}`);
      }
      
      // If successful (204), no need to return anything
    } catch (error) {
      console.error("Error in resetSession:", error);
      throw error;
    }
  }
};