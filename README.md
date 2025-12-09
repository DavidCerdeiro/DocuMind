# 🧠 DocuMind | Enterprise RAG System

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring_AI-0.8-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)

> **"Chat with your documents"**. DocuMind is a robust REST API and Interface that leverages **Generative AI** to analyze PDF documents and answer questions based on their content, ensuring zero hallucinations by strictly adhering to the provided context.

---

## 🚀 About The Project

DocuMind is a Full-Stack application designed to demonstrate the implementation of a **RAG (Retrieval-Augmented Generation)** architecture in a production-ready environment.

Unlike simple wrappers around ChatGPT, this project implements a full ETL pipeline for document processing, utilizing vector databases for semantic search and local LLMs for privacy-first inference.

### Key Features
* **📄 Smart Ingestion:** Upload PDF contracts or manuals. The system splits, cleans, and vectorizes the text automatically.
* **🔍 Semantic Search:** Uses `pgvector` to find relevant information based on meaning, not just keywords.
* **🤖 Local Intelligence:** Integrated with **Ollama** (Llama3/Mistral) to run the AI locally with zero cloud costs.
* **🛡️ Facade Pattern:** Implements a clean architecture separating the Business Logic from the AI complexity.
* **☁️ Cloud Native:** Fully containerized with Docker & Kubernetes manifests ready for deployment.

---

## 🛠️ Tech Stack & Architecture

### Backend Core
* **Language:** Java 21 (LTS) - Leveraging Virtual Threads for high concurrency.
* **Framework:** Spring Boot 3.4.
* **AI Integration:** Spring AI.
* **Design Patterns:** MVC (Model-View-Controller) + **Facade Pattern**.

### Data & Infrastructure
* **Vector DB:** PostgreSQL with `pgvector` extension.
* **LLM Engine:** Ollama (Local) or OpenAI/Groq (Cloud fallback).
* **DevOps:** Docker Compose & Kubernetes (Minikube compatible).

### Frontend
* **Library:** React + Vite.
* **Styling:** Tailwind CSS.

---

## 📐 System Architecture

The application follows a strict separation of concerns using the **Facade Pattern** to orchestrate the RAG flow.

```mermaid
graph TD
    User[👤 User] -->|1. Upload PDF / Ask Question| UI[⚛️ React Frontend]
    UI -->|2. REST API Request| Controller[Spring Boot Controller]
    Controller -->|3. Delegate| Facade[🏗️ DocuMind Facade Service]
    
    subgraph "Application Core"
        Facade -->|4. Parse & Chunk| PDFReader[PDF Reader]
        Facade -->|5. Embed & Store| VectorDB[(🐘 PostgreSQL + pgvector)]
        Facade -->|6. Retrieve Context| VectorDB
        Facade -->|7. Generate Answer| ChatClient[🤖 Spring AI Chat Client]
    end
    
    ChatClient <-->|8. Inference| Ollama[🦙 Ollama / LLM]
    
    Facade -->|9. Final Response| Controller
    Controller -->|10. JSON| UI
````
## ⚡ Getting Started
### Prerequisites

* Docker & Docker Compose

* Java 21 SDK

* Node.js 20+

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

