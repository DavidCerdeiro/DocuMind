const API_URL = "/api/docs"; 

export const DocuMindService = {
  /**
   * Upload a PDF document to the backend server.
   * @param file The PDF file to upload.
   * @throws Error if the upload fails or the format is invalid.
   */
  uploadDocument: async (file: File): Promise<void> => {
    const formData = new FormData();
    // "file" must match @RequestParam("file") in your DocumentController.java
    formData.append("file", file);

    try {
      const response = await fetch(API_URL, {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        // Specific handling for the 415 error you defined in the Controller
        if (response.status === 415) {
          throw new Error("Unsupported format. The server only accepts PDFs.");
        }
        throw new Error(`Upload error: ${response.status} ${response.statusText}`);
      }
      
      // If 200 OK, the function completes successfully (void)
    } catch (error) {
      console.error("Error in DocuMindService:", error);
      throw error;
    }
  }
};