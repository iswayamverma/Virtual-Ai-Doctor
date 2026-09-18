# 🩺 Virtual AI Doctor

An AI-powered virtual doctor web application that lets users describe their symptoms and receive intelligent medical guidance in **Hindi or English**. Built with Spring Boot, Spring AI, the Model Context Protocol (MCP), JWT authentication, and Groq AI (LLaMA) — deployed across Railway, Render, and Vercel.

🔗 **Live Demo:** [virtual-ai-doctor-frontend.vercel.app](https://virtual-ai-doctor-frontend.vercel.app)

---

## ✨ Features

- 🤖 **AI Symptom Analysis** — Powered by Groq's LLaMA 3.1 model through Spring AI, with agentic tool-calling (MCP) for dynamic, on-demand access to patient context instead of stuffing full patient data into every prompt
- 🌐 **Multilingual Support** — Automatically detects and responds in Hindi or English
- 🚨 **Agentic Severity Flagging** — The AI itself calls a `flagHighSeverity` MCP tool when it judges a consultation as an emergency, rather than the backend parsing a severity tag out of plain text
- 📧 **Email Alerts** — Sends automatic email when HIGH severity is detected
- 📄 **PDF Report Generation** — Download consultation reports as PDF
- 💊 **Nearby Pharmacy Finder** — Finds real pharmacies using OpenStreetMap + Overpass API
- 🔐 **JWT Authentication** — Secure signup/login with token-based auth
- 📋 **Consultation History** — Full history of past sessions with diagnosis and severity, backed by Spring AI's ChatMemory for persistent, multi-turn conversation context
- 👤 **Health Profile** — Age, blood group, allergies, and medical history, exposed to the AI as an on-demand MCP tool (`getPatientProfile`) rather than injected into every request
- 🌙 **Dark / Light Mode** — Theme toggle across all pages

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 17 + Spring Boot 3.5 | Core backend framework |
| Spring Security + JWT | Authentication & authorization |
| Spring AI | LLM orchestration — ChatClient, ChatMemory, tool calling |
| Model Context Protocol (MCP) | Server/client tool-calling architecture — exposes patient profile lookup and severity flagging as callable tools for the AI, built on the official MCP Java SDK |
| Spring Data JPA + Hibernate | Database ORM |
| MySQL | Production database (also stores persisted chat memory via Spring AI's JDBC chat-memory repository) |
| Groq API (LLaMA 3.1) | AI diagnosis engine, accessed via Spring AI's OpenAI-compatible client |
| Spring Mail + Brevo API | Email alert system |
| WebFlux (WebClient) | HTTP client for Brevo email API calls |

### Frontend
| Technology | Purpose |
|---|---|
| HTML5 / CSS3 / JavaScript | Core frontend |
| OpenStreetMap + Overpass API | Pharmacy finder (no API key needed) |

### Deployment
| Service | Purpose |
|---|---|
| Vercel | Frontend hosting |
| Render | Backend hosting |
| Railway | MySQL database |

---

## 📁 Project Structure

```
virtual-doctor/
├── src/main/java/com/virtualdoctor/virtual_doctor/
│   ├── config/
│   │   ├── SecurityConfig.java        # JWT + CORS + Spring Security
│   │   ├── ToolConfig.java            # Registers MCP tools with the MCP server
│   │   └── ChatClientConfig.java      # Configures Spring AI ChatClient, ChatMemory, MCP tool callbacks
│   ├── controller/
│   │   ├── AuthController.java        # /auth/register, /auth/login
│   │   ├── ConsultationController.java # /consultation/*
│   │   └── ProfileController.java     # /profile GET & PUT
│   ├── model/
│   │   ├── User.java
│   │   ├── Session.java
│   │   └── Message.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── SessionRepository.java
│   │   └── MessageRepository.java
│   ├── security/
│   │   ├── JwtFilter.java             # JWT request filter
│   │   └── JwtUtil.java               # Token generation & validation
│   ├── tool/
│   │   ├── PatientTools.java          # MCP tool: on-demand patient profile retrieval
│   │   └── SeverityTools.java         # MCP tool: agentic high-severity flagging
│   └── service/
│       ├── UserService.java           # Auth logic
│       ├── AiService.java             # Spring AI ChatClient orchestration (Groq + MCP tools + ChatMemory)
│       ├── ConsultationService.java   # Chat flow, session lifecycle, severity-triggered alerts
│       ├── ConsultationContext.java   # Request-scoped session context for MCP tool calls
│       └── EmailService.java          # High severity email alerts
├── src/main/resources/
│   └── application.properties        # Config (uses env vars)
└── Dockerfile
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL database
- Groq API key (free at [console.groq.com](https://console.groq.com))
- Gmail account (for email alerts)

### 1. Clone the repository
```bash
git clone https://github.com/Swayamverma8/Virtual-Ai-Doctor.git
cd Virtual-Ai-Doctor
```

### 2. Set environment variables
Create a `.env` file or set these in your system:
```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=virtual_doctor
DB_USER=root
DB_PASS=yourpassword
GROQ_API_KEY=your_groq_api_key
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

### 3. Run the backend
```bash
mvn spring-boot:run
```
Backend starts at `http://localhost:8080`

### 4. Run the frontend
Simply open `index.html` in your browser or serve with Live Server.
Update the `API` constant in each HTML file to point to `http://localhost:8080`.

---

## 🔌 API Endpoints

### Auth
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Register new user | ❌ Public |
| POST | `/auth/login` | Login & get JWT token | ❌ Public |

### Profile
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/profile` | Get user profile | ✅ Required |
| PUT | `/profile` | Update health profile | ✅ Required |

### Consultation
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/consultation/start` | Start new session | ✅ Required |
| POST | `/consultation/chat/{sessionId}` | Send message to AI (routed through Spring AI + MCP tool calling) | ✅ Required |
| GET | `/consultation/history` | Get all past sessions | ✅ Required |
| GET | `/consultation/messages/{sessionId}` | Get session messages | ✅ Required |

---

## 🌐 Deployment

### Backend (Render)
1. Connect your GitHub repo to Render
2. Set all environment variables in Render dashboard
3. Build command: `mvn clean package -DskipTests`
4. Start command: `java -jar target/*.jar`

### Database (Railway)
1. Create a MySQL service on Railway
2. Copy the connection details to Render environment variables

### Frontend (Vercel)
1. Connect your frontend GitHub repo to Vercel
2. No build step needed — static HTML deployment

---

## 📸 Screenshots

> Dashboard, Chat, and Pharmacy pages

| Dashboard | AI Chat | Pharmacy Finder |
|---|---|---|
| ![Dashboard](https://placehold.co/300x180/0a1628/00c896?text=Dashboard) | ![Chat](https://placehold.co/300x180/0a1628/00c896?text=AI+Chat) | ![Pharmacy](https://placehold.co/300x180/0a1628/00c896?text=Pharmacy) |

---

## 🔮 Future Plans

- [ ] Rebuild frontend in React
- [ ] Add WebSocket for real-time chat
- [ ] Android app (Android Studio)
- [ ] Voice input for symptoms
- [ ] Doctor appointment booking
- [ ] CI/CD with GitHub Actions + Docker
- [ ] MEDIUM-severity MCP tool, complementing the existing HIGH-severity flag

---

## 👨‍💻 Author

**Swayam Verma**
- GitHub: [@Swayamverma8](https://github.com/Swayamverma8)
- LinkedIn: [swayamverma](https://www.linkedin.com/in/swayamverma/)
- LeetCode: [thisisswayamverma](https://leetcode.com/u/thisisswayamverma/)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).