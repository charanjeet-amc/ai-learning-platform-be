package com.ailearning.platform.controller;

import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.Module;
import com.ailearning.platform.entity.enums.ContentType;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import com.ailearning.platform.entity.enums.UserRole;
import com.ailearning.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class SeedController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final TopicRepository topicRepository;
    private final ConceptRepository conceptRepository;
    private final LearningUnitRepository learningUnitRepository;

    @PostMapping("/seed")
    @Transactional
    public ResponseEntity<Map<String, Object>> seedData() {
        if (courseRepository.count() > 0) {
            return ResponseEntity.ok(Map.of("message", "Data already seeded", "courses", courseRepository.count()));
        }

        // Create instructor user
        User instructor = User.builder()
                .keycloakId("seed-instructor-001")
                .email("dr.sarah.chen@ailearning.com")
                .username("dr.sarah.chen")
                .fullName("Dr. Sarah Chen")
                .role(UserRole.INSTRUCTOR)
                .totalXp(50000L)
                .build();
        instructor = userRepository.save(instructor);

        List<String> courseTitles = new ArrayList<>();

        // Course 1: Machine Learning Fundamentals
        courseTitles.add(createMLCourse(instructor));
        // Course 2: Deep Learning
        courseTitles.add(createDLCourse(instructor));
        // Course 3: NLP
        courseTitles.add(createNLPCourse(instructor));
        // Course 4: Data Science
        courseTitles.add(createDSCourse(instructor));
        // Course 5: Generative AI
        courseTitles.add(createGenAICourse(instructor));

        return ResponseEntity.ok(Map.of(
            "message", "Seed data created successfully",
            "courses", courseTitles,
            "instructor", instructor.getFullName()
        ));
    }

    private String createMLCourse(User instructor) {
        Course course = Course.builder()
                .title("Machine Learning Fundamentals")
                .description("Master the core principles of machine learning from scratch. Covers supervised and unsupervised learning, model evaluation, feature engineering, and real-world applications with Python and scikit-learn.")
                .shortDescription("Learn ML from zero to hero with hands-on Python projects")
                .difficulty(DifficultyLevel.BEGINNER)
                .tags(new String[]{"Machine Learning", "Python", "scikit-learn", "Data Science"})
                .createdBy(instructor)
                .published(true)
                .rating(4.8)
                .enrollmentCount(12540L)
                .estimatedDurationMinutes(2400)
                .build();
        course = courseRepository.save(course);

        // Module 1
        Module m1 = Module.builder().course(course).title("Introduction to Machine Learning")
                .description("Understand what ML is, its types, and real-world use cases.")
                .orderIndex(0).build();
        m1 = moduleRepository.save(m1);

        Topic t1 = Topic.builder().module(m1).title("What is Machine Learning?")
                .orderIndex(0).build();
        t1 = topicRepository.save(t1);

        Concept c1 = Concept.builder().topic(t1).title("Definition of Machine Learning")
                .definition("ML is a subset of AI that enables systems to learn from experience without explicit programming.")
                .difficultyLevel(DifficultyLevel.BEGINNER).orderIndex(0).build();
        c1 = conceptRepository.save(c1);

        saveLearningUnit(c1, "What is Machine Learning?", ContentType.TEXT,
                "Machine learning (ML) is a branch of AI focused on building systems that learn from data. Unlike traditional programming where you write explicit rules, ML algorithms discover patterns and make decisions with minimal human intervention.\n\n**Key Idea:** Instead of coding rules, you provide data and let the algorithm find the rules.\n\nTom Mitchell's Definition: A program learns from experience E with respect to task T and performance measure P, if its performance at T, measured by P, improves with experience E.");

        Concept c2 = Concept.builder().topic(t1).title("Types of Machine Learning")
                .definition("The three main paradigms: supervised, unsupervised, and reinforcement learning.")
                .difficultyLevel(DifficultyLevel.BEGINNER).orderIndex(1).build();
        c2 = conceptRepository.save(c2);

        saveLearningUnit(c2, "Three Paradigms of ML", ContentType.TEXT,
                "1. **Supervised Learning** — learns from labeled data (classification, regression)\n2. **Unsupervised Learning** — finds patterns in unlabeled data (clustering, dimensionality reduction)\n3. **Reinforcement Learning** — agent learns by interacting with environment and receiving rewards");

        // Module 2
        Module m2 = Module.builder().course(course).title("Supervised Learning Algorithms")
                .description("Deep dive into classification and regression.").orderIndex(1).build();
        m2 = moduleRepository.save(m2);

        Topic t2 = Topic.builder().module(m2).title("Linear Regression")
                .orderIndex(0).build();
        t2 = topicRepository.save(t2);

        Concept c3 = Concept.builder().topic(t2).title("Simple Linear Regression")
                .definition("Modeling the relationship between a feature and target using y = mx + b.")
                .difficultyLevel(DifficultyLevel.BEGINNER).orderIndex(0).build();
        c3 = conceptRepository.save(c3);

        saveLearningUnit(c3, "Understanding Linear Regression", ContentType.TEXT,
                "Linear Regression fits a line through data: y = mx + b.\n\nWe minimize Mean Squared Error (MSE) using Gradient Descent:\n1. Initialize weights randomly\n2. Calculate predictions\n3. Compute gradients\n4. Update weights\n5. Repeat until convergence\n\nPython: `from sklearn.linear_model import LinearRegression`");

        return course.getTitle();
    }

    private String createDLCourse(User instructor) {
        Course course = Course.builder()
                .title("Deep Learning with Neural Networks")
                .description("From perceptrons to transformers. Master backpropagation, CNNs, RNNs, attention mechanisms, and modern architectures. Hands-on with PyTorch.")
                .shortDescription("From perceptrons to transformers — master deep learning with PyTorch")
                .difficulty(DifficultyLevel.MEDIUM)
                .tags(new String[]{"Deep Learning", "Neural Networks", "PyTorch", "CNN", "Transformers"})
                .createdBy(instructor)
                .published(true)
                .rating(4.9)
                .enrollmentCount(8930L)
                .estimatedDurationMinutes(3600)
                .build();
        course = courseRepository.save(course);

        Module m1 = Module.builder().course(course).title("Neural Network Foundations")
                .description("Build intuition for how neural networks learn.").orderIndex(0).build();
        m1 = moduleRepository.save(m1);

        Topic t1 = Topic.builder().module(m1).title("Perceptron and Activation Functions")
                .orderIndex(0).build();
        t1 = topicRepository.save(t1);

        Concept c1 = Concept.builder().topic(t1).title("The Perceptron Model")
                .definition("The simplest neural network: a single neuron making binary decisions.")
                .difficultyLevel(DifficultyLevel.BEGINNER).orderIndex(0).build();
        c1 = conceptRepository.save(c1);

        saveLearningUnit(c1, "Understanding the Perceptron", ContentType.TEXT,
                "The perceptron takes inputs, multiplies by weights, sums, and applies a step function.\n\nLimitation: Can only learn linearly separable patterns (cannot learn XOR).\nSolution: Stack multiple layers → Multi-Layer Perceptron (MLP).");

        Concept c2 = Concept.builder().topic(t1).title("Activation Functions")
                .definition("Non-linear functions enabling neural networks to learn complex patterns.")
                .difficultyLevel(DifficultyLevel.MEDIUM).orderIndex(1).build();
        c2 = conceptRepository.save(c2);

        saveLearningUnit(c2, "Activation Functions Deep Dive", ContentType.TEXT,
                "ReLU: f(x) = max(0, x) — Default for hidden layers\nSigmoid: output (0,1) — Binary classification\nTanh: output (-1,1) — Zero-centered\nSoftmax: probability distribution — Multi-class output");

        return course.getTitle();
    }

    private String createNLPCourse(User instructor) {
        Course course = Course.builder()
                .title("Natural Language Processing with Transformers")
                .description("From text preprocessing to GPT. Cover tokenization, embeddings, attention mechanisms, transformers, BERT, GPT, and fine-tuning large language models.")
                .shortDescription("Master NLP from tokenization to fine-tuning GPT and BERT")
                .difficulty(DifficultyLevel.HARD)
                .tags(new String[]{"NLP", "Transformers", "BERT", "GPT", "LLM"})
                .createdBy(instructor)
                .published(true)
                .rating(4.7)
                .enrollmentCount(6720L)
                .estimatedDurationMinutes(3000)
                .build();
        course = courseRepository.save(course);

        Module m1 = Module.builder().course(course).title("Attention Mechanisms and Transformers")
                .description("The architecture that revolutionized NLP and AI.").orderIndex(0).build();
        m1 = moduleRepository.save(m1);

        Topic t1 = Topic.builder().module(m1).title("Self-Attention Mechanism")
                .orderIndex(0).build();
        t1 = topicRepository.save(t1);

        Concept c1 = Concept.builder().topic(t1).title("Self-Attention and Query-Key-Value")
                .definition("How transformers weigh importance of different parts of input sequences.")
                .difficultyLevel(DifficultyLevel.HARD).orderIndex(0).build();
        c1 = conceptRepository.save(c1);

        saveLearningUnit(c1, "Self-Attention Explained", ContentType.TEXT,
                "For each token, compute Query (what am I looking for?), Key (what do I contain?), Value (what info do I provide?).\n\nAttention(Q,K,V) = softmax(QK^T / sqrt(d_k)) × V\n\nMulti-head attention uses multiple heads to capture different relationship types (syntax, semantics, coreference).");

        return course.getTitle();
    }

    private String createDSCourse(User instructor) {
        Course course = Course.builder()
                .title("Data Science with Python")
                .description("Complete data science toolkit. Master NumPy, Pandas, Matplotlib, Seaborn, statistical analysis, EDA, hypothesis testing, and storytelling with data.")
                .shortDescription("The complete Python data science toolkit — from EDA to storytelling")
                .difficulty(DifficultyLevel.BEGINNER)
                .tags(new String[]{"Python", "Pandas", "NumPy", "Data Visualization", "Statistics"})
                .createdBy(instructor)
                .published(true)
                .rating(4.6)
                .enrollmentCount(18200L)
                .estimatedDurationMinutes(2100)
                .build();
        course = courseRepository.save(course);

        Module m1 = Module.builder().course(course).title("NumPy and Pandas Essentials")
                .description("Master the foundational libraries for data manipulation.").orderIndex(0).build();
        m1 = moduleRepository.save(m1);

        Topic t1 = Topic.builder().module(m1).title("Pandas DataFrame Operations")
                .orderIndex(0).build();
        t1 = topicRepository.save(t1);

        Concept c1 = Concept.builder().topic(t1).title("DataFrame Fundamentals")
                .definition("Creating, indexing, filtering, and transforming DataFrames.")
                .difficultyLevel(DifficultyLevel.BEGINNER).orderIndex(0).build();
        c1 = conceptRepository.save(c1);

        saveLearningUnit(c1, "Mastering Pandas DataFrames", ContentType.TEXT,
                "Pandas DataFrame is the primary data structure for data science.\n\nKey operations: column selection, boolean filtering, handling missing data (fillna, dropna), and groupby aggregations.\n\nPro tips: use .loc[] for label-based and .iloc[] for position-based indexing.");

        return course.getTitle();
    }

    private String createGenAICourse(User instructor) {
        Course course = Course.builder()
                .title("Generative AI and Prompt Engineering")
                .description("Understand and harness generative AI. Learn how LLMs work, master prompt engineering, build RAG systems, use function calling, and create AI agents.")
                .shortDescription("Master LLMs, prompt engineering, RAG, and AI agents")
                .difficulty(DifficultyLevel.MEDIUM)
                .tags(new String[]{"Generative AI", "LLM", "Prompt Engineering", "RAG", "AI Agents"})
                .createdBy(instructor)
                .published(true)
                .rating(4.9)
                .enrollmentCount(22100L)
                .estimatedDurationMinutes(1800)
                .build();
        course = courseRepository.save(course);

        Module m1 = Module.builder().course(course).title("How Large Language Models Work")
                .description("Demystify the technology behind ChatGPT, Claude, and Gemini.").orderIndex(0).build();
        m1 = moduleRepository.save(m1);

        Topic t1 = Topic.builder().module(m1).title("LLM Architecture and Training")
                .orderIndex(0).build();
        t1 = topicRepository.save(t1);

        Concept c1 = Concept.builder().topic(t1).title("Next-Token Prediction")
                .definition("The fundamental mechanism by which LLMs generate coherent text.")
                .difficultyLevel(DifficultyLevel.MEDIUM).orderIndex(0).build();
        c1 = conceptRepository.save(c1);

        saveLearningUnit(c1, "How LLMs Generate Text", ContentType.TEXT,
                "LLMs are next-token predictors trained on vast text.\n\nTraining pipeline:\n1. Pretraining — massive text corpora, predict next token\n2. SFT — instruction-response pairs\n3. RLHF — human feedback alignment\n\nTemperature controls randomness: 0 = deterministic, 1 = full distribution sampling.");

        Module m2 = Module.builder().course(course).title("Prompt Engineering Techniques")
                .description("Master the art and science of crafting effective prompts.").orderIndex(1).build();
        m2 = moduleRepository.save(m2);

        Topic t2 = Topic.builder().module(m2).title("Core Prompting Techniques")
                .orderIndex(0).build();
        t2 = topicRepository.save(t2);

        Concept c2 = Concept.builder().topic(t2).title("Chain-of-Thought Prompting")
                .definition("Unlocking reasoning by asking models to think step by step.")
                .difficultyLevel(DifficultyLevel.MEDIUM).orderIndex(0).build();
        c2 = conceptRepository.save(c2);

        saveLearningUnit(c2, "Chain-of-Thought Prompting", ContentType.TEXT,
                "CoT dramatically improves reasoning. Just add 'Let's think step by step.'\n\nTechniques: Zero-shot CoT, Few-shot CoT, Self-Consistency (majority vote), Tree of Thought.\n\nBest for: math, logic, multi-step reasoning, code debugging.");

        return course.getTitle();
    }

    private void saveLearningUnit(Concept concept, String title, ContentType type, String body) {
        Map<String, Object> content = new HashMap<>();
        content.put("body", body);
        LearningUnit unit = LearningUnit.builder()
                .concept(concept)
                .title(title)
                .type(type)
                .content(content)
                .orderIndex(0)
                .build();
        learningUnitRepository.save(unit);
    }
}
