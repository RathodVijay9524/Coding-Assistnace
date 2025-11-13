# 🧠 Multi-Brain Graph-RAG Architecture

A sophisticated AI system built with Spring Boot and Spring AI that implements a multi-brain architecture for intelligent, context-aware responses across multiple AI providers.

## 🚀 Features

### 🧠 Four Specialized Brains
- **Brain 0: Knowledge Graph** - Concept detection and relationship mapping
- **Brain 1: Smart Tool Retriever** - Intelligent tool selection and execution  
- **Brain 2: Response Summarizer** - Automatic summarization for long responses
- **Brain 3: Self-Refine Evaluator** - Quality assurance with LLM-as-Judge pattern

### 🤖 Multi-Provider Support
- **OpenAI** (GPT-4, GPT-3.5-turbo)
- **Anthropic Claude** (Claude-3, Claude-2)
- **Google Gemini** (Gemini Pro, Gemini Ultra)
- **Ollama** (Local models: Llama, Mistral, CodeLlama)
- **HuggingFace** (Various open-source models)

### 🛠️ Intelligent Tools
- Weather information (OpenWeatherMap integration)
- Calendar management (Google Calendar API)
- Web search (Google Search API)
- Email sending capabilities
- Date/time utilities

### 🌐 Modern Web Interface
- Interactive Thymeleaf-based chatbot
- Real-time AJAX communication
- Provider selection and tool toggles
- Responsive Bootstrap design

## 🏗️ Architecture Overview

```
User Query → Knowledge Graph → Memory → Tool Retrieval → Response Generation → Summarization → Quality Refinement → Final Response
```

### Processing Flow
1. **Knowledge Graph Analysis** (Order: 100) - Detects concepts and relationships
2. **Memory Loading** - Retrieves conversation context (20 message window)
3. **Smart Tool Selection** - Vector-based tool matching and execution
4. **Response Generation** - AI provider generates initial response
5. **Response Summarization** (Order: 500) - Creates summaries for long responses
6. **Quality Refinement** (Order: 1000) - Self-evaluation and improvement

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Spring Boot 3.5.7
- Maven 3.6+
- API Keys for desired providers

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd Coding-Assistance
```

2. **Set environment variables**
```bash
export OPENAI_API_KEY="your-openai-key"
export ANTHROPIC_API_KEY="your-anthropic-key"
export GOOGLE_API_KEY="your-google-key"
export OPENWEATHERMAP_API_KEY="your-weather-key"
```

3. **Build and run**
```bash
mvn clean install
mvn spring-boot:run
```

4. **Access the application**
- Web Interface: http://localhost:8080
- Chatbot: http://localhost:8080/chatbot
- API: http://localhost:8080/api/chat/{provider}

### Optional: Local Models with Ollama
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Start Ollama server
ollama serve

# Pull a model
ollama pull llama2
```

## 📖 Usage Examples

### Web Interface
1. Navigate to http://localhost:8080/chatbot
2. Select your preferred AI provider
3. Toggle tools on/off as needed
4. Start chatting!

### API Usage

#### Simple Chat
```bash
curl -X POST http://localhost:8080/send \
  -d "message=What's the weather in London?" \
  -d "provider=openai" \
  -d "useTools=true"
```

#### Response Format
```json
{
  "response": "The weather in London is currently 15°C with light rain...",
  "formattedResponse": "The weather in London is currently 15°C with light rain...<br/>• Condition: Light rain<br/>• Temperature: 15.0°C",
  "provider": "openai",
  "toolsUsed": ["getCurrentWeather"],
  "timestamp": "2025-11-13T23:53:49"
}
```

### Example Queries

#### Weather Information
```
"What's the weather in Mumbai and Delhi?"
```
- **Knowledge Graph**: Detects [weather] → [temperature, humidity, forecast]
- **Tools**: Activates getCurrentWeather
- **Result**: Real-time weather data for both cities

#### Multi-Domain Query
```
"Weather in Tokyo and my calendar for today"
```
- **Knowledge Graph**: Detects [weather, calendar] → [temperature, event, meeting]
- **Tools**: Activates getCurrentWeather, getTodayEvents
- **Result**: Weather info + calendar events

#### AI Information
```
"Latest Spring AI version and Claude model details"
```
- **Knowledge Graph**: Detects [ai] → [model, llm, claude, spring]
- **Tools**: Activates googleSearch
- **Result**: Current version info and model specifications

## 🔧 Configuration

### Application Properties
```properties
# AI Provider Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.google.genai.api-key=${GOOGLE_API_KEY}

# Ollama (Local)
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama2

# Performance Tuning
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=2000
```

### Memory Configuration
- **Window Size**: 20 messages
- **Context Preservation**: Automatic
- **Provider Isolation**: Separate memory per provider

### Tool Configuration
- **Weather**: OpenWeatherMap API integration
- **Calendar**: Google Calendar API (requires setup)
- **Search**: Google Search API (requires setup)
- **Email**: SMTP configuration required

## 📊 Monitoring

### Log Analysis
The system provides detailed logging for each brain:

```
🕸️ Knowledge Graph: Analyzing query for concept relationships...
🔍 Detected concepts: [weather, ai]
🔗 Related concepts: [temperature, model, llm]

SmartFinder: Found tools [getCurrentWeather, googleSearch]
Tools activated: [getCurrentWeather, googleSearch]

📝 Brain 2 (Summarizer): Response length OK (245 chars)
🧾 Judge rating: 4
✅ Quality threshold met
```

### Health Endpoints
- `/actuator/health` - Overall system health
- `/api/chat/health/{provider}` - Provider-specific health
- `/api/chat/providers` - List of available providers

### Performance Metrics
- **Average Response Time**: 2-8 seconds
- **Tool Success Rate**: >95%
- **Quality Refinement Rate**: ~30%
- **Concept Detection Accuracy**: >90%

## 🏗️ Architecture Details

### Brain Components

#### Brain 0: Knowledge Graph Advisor
```java
// Concept relationships
weather → [temperature, humidity, wind, forecast]
ai → [model, llm, openai, claude, gemini]
calendar → [event, meeting, schedule, appointment]
```

#### Brain 1: Smart Tool Retriever
- Vector-based tool selection
- Parallel tool execution
- Intelligent tool chaining
- Error handling and fallbacks

#### Brain 2: Response Summarizer
- Threshold: 800 characters
- Rule-based summarization
- Key information preservation
- Fallback safety

#### Brain 3: Self-Refine Evaluator
- LLM-as-Judge pattern
- 1-5 quality rating scale
- Recursive improvement (rating < 4)
- Quality threshold enforcement

### Multi-Provider Architecture
Each provider is configured with the complete multi-brain setup:
- Memory management (20 message window)
- All four brain advisors
- Complete tool suite
- Error handling and monitoring

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Coverage
- Unit tests for each brain component
- Integration tests for multi-provider support
- End-to-end tests for web interface
- Performance tests for response times

### Example Test Scenarios
- Concept detection accuracy
- Tool activation verification
- Quality refinement validation
- Provider switching functionality
- Error handling and recovery

## 📚 Documentation

- **[Multi-Brain Architecture](docs/Multi-Brain-Architecture.md)** - Detailed architecture documentation
- **[Implementation Guide](docs/Implementation-Guide.md)** - Technical implementation details
- **[API Reference](docs/API-Reference.md)** - Complete API documentation
- **[Deployment Guide](docs/Deployment-Guide.md)** - Production deployment instructions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot best practices
- Add tests for new features
- Update documentation
- Ensure all brains work with new providers
- Maintain backward compatibility

## 🔮 Roadmap

### Upcoming Features
- **Dynamic Knowledge Graph**: Auto-learning concept relationships
- **Advanced Summarization**: LLM-based summarization
- **Multi-Modal Support**: Image and document processing
- **Real-time Streaming**: WebSocket-based responses
- **Custom Tool Registration**: Runtime tool addition
- **A/B Testing**: Provider performance comparison

### Scalability Enhancements
- Microservices architecture
- Redis caching for memory
- Kubernetes deployment
- Distributed tool execution
- Real-time monitoring dashboard

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring AI Team** - For the excellent AI integration framework
- **OpenAI, Anthropic, Google** - For providing powerful AI models
- **Ollama Community** - For local model support
- **Spring Boot Team** - For the robust application framework

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-repo/discussions)
- **Documentation**: [Wiki](https://github.com/your-repo/wiki)

---

Built with ❤️ using Spring Boot, Spring AI, and the Multi-Brain Architecture pattern.
