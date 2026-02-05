# 🧠 DocuMind | Enterprise RAG System

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring_AI-0.8-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)

> **"Chat with your documents"**. DocuMind is a robust REST API and Interface that leverages **Generative AI** to analyze PDF documents and answer questions based on their content, ensuring zero hallucinations by strictly adhering to the provided context. Unlike basic RAG demos, DocuMind implements an **Asynchronous Architecture** to handle large files (e.g., +200-page manuals) without blocking the user interface, ensuring a robust backend performance.

---
## 🎥 Live Demo
https://github.com/user-attachments/assets/12b24941-81a3-459c-b040-5bc3e8795f2b
> **Note regarding performance:** This demo has been edited for brevity. The document ingestion and AI inference phases have been accelerated to demonstrate the UI flow. 
> Actual processing times will vary depending on your local hardware resources (CPU/GPU) allocated to the Ollama instance.

## 🚀 About The Project

DocuMind is a Full-Stack application designed to demonstrate the implementation of a **RAG (Retrieval-Augmented Generation)** architecture in a production-ready environment.

Unlike simple wrappers around ChatGPT, this project implements a full ETL pipeline for document processing, utilizing vector databases for semantic search and local LLMs for privacy-first inference.

### Key Features
* **⚡ Async "Fire-and-Forget" Ingestion:** Uploads are handled asynchronously. The server accepts the file immediately (`202 Accepted`) and processes chunking and embedding in the background, preventing browser timeouts on large files.
* **📊 Real-Time Status Polling:** The frontend implements smart polling to track the document processing progress (0% to 100%) in real-time with visual feedback.
* **🧠 Local Intelligence (No Cloud Costs):** Powered by **Phi-3 (3.8B)** and **Nomic Embed Text**, running locally via Ollama.
* **🎯 Precision Tuned:**
    * **Context Window:** Optimized `300-token` chunks to reduce noise.
    * **Retrieval:** `Top-K: 6` retrieval to capture dispersed information (e.g., financial tables) without sacrificing speed.
    * **Determinism:** `Temperature: 0.1` for strictly factual answers.
* **🛡️ Hallucination Safety Net:** A robust prompting strategy that forces the model to reply `[[NO_INFO_FOUND]]` if the answer isn't in the text, preventing misleading information.
* **🌍 Multi-Language Support:** Automatically detects the user's question language (English/Spanish) and responds in the same language, regardless of the document's original language.

---

## 🛠️ Tech Stack & Architecture

### Backend Core
* **Language:** Java 21 (LTS) - Leveraging Virtual Threads for high concurrency.
* **Framework:** Spring Boot 3.5.
* **AI Integration:** Spring AI.
* **Design Patterns:** MVC (Model-View-Controller) + **Facade Pattern**.

### Data & Infrastructure
* **Vector DB:** PostgreSQL with `pgvector` extension.
* **LLM Engine:** Ollama (Local), phi3 model.
* **DevOps:** Docker Compose & Kubernetes (Minikube compatible).

### Frontend
* **Library:** React + Vite.
* **Styling:** Tailwind CSS.

---

## 📐 System Architecture

The application follows a strict separation of concerns using the **Facade Pattern** to orchestrate the RAG flow.

```mermaid
graph TD
    User["👤 User"] -->|"1. Upload PDF"| UI["⚛️ React Frontend"]
    UI -->|"2. POST /api/docs/upload"| Controller
    Controller -->|"3. Return JobID (202 Accepted)"| UI
    
    subgraph "Async Background Process"
        Controller -.->|"4. Trigger Async Job"| Service
        Service -->|"5. Clean & Chunk (300 tokens)"| PDFReader
        Service -->|"6. Batch Embedding (50/batch)"| VectorDB[("🐘 PostgreSQL / pgvector")]
        Service -- "Update Status" --> StatusMap["📍 Status Tracker"]
    end
    
    subgraph "Status Check"
        UI -->|"7. GET /status/{jobId}"| Controller
        Controller -->|"8. Return Status"| UI
    end
    
    UI -->|"9. Chat Interaction"| ChatClient
    ChatClient <-->|"10. RAG Inference"| Ollama["🦙 Ollama (Phi-3)"]
````
## ⚡ Getting Started
### Prerequisites

* Docker & Docker Compose

* 8GB+ RAM recommended

### 1. Clone the repository
```shell
git clone [https://github.com/DavidCerdeiro/DocuMind.git](https://github.com/DavidCerdeiro/DocuMind.git)
cd DocuMind
````

### 2. Run with Docker Compose (Recommended)

This command starts PostgreSQL (pgvector), Ollama, Backend, and Frontend.

````shell
docker-compose up --build
````

### 3. Access the Application

* Frontend: http://localhost:5173

* API Docs (Swagger): http://localhost:8080/swagger-ui.html

## 🧪 Testing & Quality

* Unit Testing: JUnit 5 & Mockito for Service layer.

* Integration Testing: Testcontainers used to spin up a real PostgreSQL instance during tests to verify Vector Similarity Search.

## 📂 Project Structure

```
documind/
├── backend/
│   ├── src/main/java/com/documind/
│   │   ├── controller/      # REST Endpoints (MVC)
│   │   ├── service/         # Facade & Business Logic
│   │   ├── model/           # DTOs (Records) & Entities
│   │   └── rag/             # AI & Vector Store Components
│   └── Dockerfile
├── frontend/                # React Application
├── k8s/                     # Kubernetes Manifests
└── docker-compose.yml
````

## 📬 Contact

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/david-jes%C3%BAs-cerdeiro-gallardo-0b1123284/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:davidcergall22@gmail.com)

