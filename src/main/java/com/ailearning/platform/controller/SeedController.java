package com.ailearning.platform.controller;

import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.Module;
import com.ailearning.platform.entity.enums.ContentType;
import com.ailearning.platform.entity.enums.CourseStatus;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import com.ailearning.platform.entity.enums.QuestionType;
import com.ailearning.platform.entity.enums.UserRole;
import com.ailearning.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/seed-questions")
    @Transactional
    public ResponseEntity<Map<String, Object>> seedQuestions() {
        if (questionRepository.count() > 0) {
            return ResponseEntity.ok(Map.of("message", "Questions already seeded", "count", questionRepository.count()));
        }

        int count = 0;
        List<Concept> allConcepts = conceptRepository.findAll();
        Map<String, Concept> byCTitle = new HashMap<>();
        for (Concept c : allConcepts) { byCTitle.put(c.getTitle(), c); }

        Concept c;
        c = byCTitle.get("Definition of Machine Learning");
        if (c != null) {
            saveQuestion(c, "What is the key idea behind machine learning?",
                    List.of("Writing explicit rules for every scenario", "Letting algorithms discover patterns from data", "Manually classifying each data point", "Using only statistical formulas"),
                    "Letting algorithms discover patterns from data", DifficultyLevel.BEGINNER,
                    "ML is about providing data and letting algorithms find patterns, rather than writing explicit rules.");
            saveQuestion(c, "According to Tom Mitchell's definition, what must improve for a program to be considered 'learning'?",
                    List.of("Code complexity", "Performance at a task measured by P, with experience E", "Number of rules written", "Processing speed"),
                    "Performance at a task measured by P, with experience E", DifficultyLevel.BEGINNER,
                    "Mitchell's definition states that learning occurs when performance P at task T improves with experience E.");
            count += 2;
        }

        c = byCTitle.get("Types of Machine Learning");
        if (c != null) {
            saveQuestion(c, "Which type of ML learns from labeled data?",
                    List.of("Unsupervised Learning", "Reinforcement Learning", "Supervised Learning", "Semi-supervised Learning"),
                    "Supervised Learning", DifficultyLevel.BEGINNER,
                    "Supervised learning uses labeled training data (input-output pairs) to learn a mapping function.");
            saveQuestion(c, "What is an example of unsupervised learning?",
                    List.of("Image classification", "Spam detection", "Customer segmentation via clustering", "Self-driving car rewards"),
                    "Customer segmentation via clustering", DifficultyLevel.BEGINNER,
                    "Clustering is an unsupervised technique that groups similar data points without labeled examples.");
            count += 2;
        }

        c = byCTitle.get("Simple Linear Regression");
        if (c != null) {
            saveQuestion(c, "What loss function does linear regression typically minimize?",
                    List.of("Cross-entropy loss", "Mean Squared Error (MSE)", "Hinge loss", "Log loss"),
                    "Mean Squared Error (MSE)", DifficultyLevel.BEGINNER,
                    "Linear regression minimizes MSE — the average of squared differences between predicted and actual values.");
            saveQuestion(c, "In the equation y = mx + b, what does 'm' represent?",
                    List.of("The y-intercept", "The slope (weight)", "The input feature", "The prediction error"),
                    "The slope (weight)", DifficultyLevel.BEGINNER,
                    "In y = mx + b, 'm' is the slope or weight that determines how much x influences y.");
            count += 2;
        }

        c = byCTitle.get("The Perceptron Model");
        if (c != null) {
            saveQuestion(c, "What is a key limitation of a single perceptron?",
                    List.of("It cannot process numbers", "It can only learn linearly separable patterns", "It requires GPU acceleration", "It needs millions of parameters"),
                    "It can only learn linearly separable patterns", DifficultyLevel.BEGINNER,
                    "A single perceptron can only classify linearly separable data — it famously cannot learn the XOR function.");
            saveQuestion(c, "What is the solution to the perceptron's inability to learn XOR?",
                    List.of("Use a larger learning rate", "Stack multiple layers (MLP)", "Use unsupervised learning", "Use a different activation function alone"),
                    "Stack multiple layers (MLP)", DifficultyLevel.MEDIUM,
                    "By stacking perceptrons into multiple layers (Multi-Layer Perceptron), the network can learn non-linear decision boundaries.");
            count += 2;
        }

        c = byCTitle.get("Activation Functions");
        if (c != null) {
            saveQuestion(c, "Which activation function is the default choice for hidden layers in modern neural networks?",
                    List.of("Sigmoid", "Tanh", "ReLU", "Softmax"),
                    "ReLU", DifficultyLevel.MEDIUM,
                    "ReLU (Rectified Linear Unit) is the default for hidden layers because it avoids the vanishing gradient problem and is computationally efficient.");
            saveQuestion(c, "Which activation function outputs a probability distribution over multiple classes?",
                    List.of("ReLU", "Sigmoid", "Tanh", "Softmax"),
                    "Softmax", DifficultyLevel.MEDIUM,
                    "Softmax converts a vector of values into a probability distribution, making it ideal for multi-class classification output layers.");
            count += 2;
        }

        c = byCTitle.get("Self-Attention and Query-Key-Value");
        if (c != null) {
            saveQuestion(c, "In self-attention, what does the Query vector represent?",
                    List.of("What information a token provides", "What a token is looking for", "The importance weight of a token", "The position of a token"),
                    "What a token is looking for", DifficultyLevel.HARD,
                    "The Query (Q) vector represents what a token is searching for — it's compared against Key vectors of all other tokens to compute attention weights.");
            saveQuestion(c, "Why is the attention score divided by sqrt(d_k)?",
                    List.of("To make computation faster", "To prevent dot products from growing too large, stabilizing gradients", "To normalize the output between 0 and 1", "To reduce memory usage"),
                    "To prevent dot products from growing too large, stabilizing gradients", DifficultyLevel.HARD,
                    "Dividing by sqrt(d_k) prevents the dot products from becoming very large in high dimensions, which would push softmax into regions with tiny gradients.");
            count += 2;
        }

        c = byCTitle.get("DataFrame Fundamentals");
        if (c != null) {
            saveQuestion(c, "Which Pandas method is used for label-based indexing?",
                    List.of(".iloc[]", ".loc[]", ".ix[]", ".at[]"),
                    ".loc[]", DifficultyLevel.BEGINNER,
                    ".loc[] is used for label-based indexing (by row/column labels), while .iloc[] is for integer-position based indexing.");
            saveQuestion(c, "What does the groupby() method in Pandas do?",
                    List.of("Sorts the DataFrame", "Splits data into groups based on criteria for aggregation", "Merges two DataFrames", "Filters null values"),
                    "Splits data into groups based on criteria for aggregation", DifficultyLevel.BEGINNER,
                    "groupby() splits the DataFrame into groups, applies a function (like mean, sum), and combines results — the split-apply-combine pattern.");
            count += 2;
        }

        c = byCTitle.get("Next-Token Prediction");
        if (c != null) {
            saveQuestion(c, "What is the fundamental mechanism by which LLMs generate text?",
                    List.of("Rule-based grammar parsing", "Next-token prediction", "Template filling", "Database lookup"),
                    "Next-token prediction", DifficultyLevel.MEDIUM,
                    "LLMs generate text by predicting the most likely next token given the preceding context, one token at a time.");
            saveQuestion(c, "What does the temperature parameter control in LLM text generation?",
                    List.of("The speed of generation", "The randomness/creativity of output", "The maximum length of output", "The language of output"),
                    "The randomness/creativity of output", DifficultyLevel.MEDIUM,
                    "Temperature controls randomness: 0 makes output deterministic (greedy), while higher values increase diversity by flattening the probability distribution.");
            count += 2;
        }

        c = byCTitle.get("Chain-of-Thought Prompting");
        if (c != null) {
            saveQuestion(c, "What is the simplest way to trigger chain-of-thought reasoning in an LLM?",
                    List.of("Provide many examples", "Add 'Let's think step by step' to the prompt", "Increase the temperature to 1.0", "Use a larger model"),
                    "Add 'Let's think step by step' to the prompt", DifficultyLevel.MEDIUM,
                    "Zero-shot CoT can be triggered by simply appending 'Let's think step by step' — this encourages the model to show its reasoning process.");
            saveQuestion(c, "Which type of tasks benefit most from chain-of-thought prompting?",
                    List.of("Simple factual recall", "Text translation", "Math, logic, and multi-step reasoning", "Image generation"),
                    "Math, logic, and multi-step reasoning", DifficultyLevel.MEDIUM,
                    "CoT is most beneficial for tasks requiring sequential reasoning — math problems, logical deductions, and multi-step problem solving.");
            count += 2;
        }

        // ── SUBJECTIVE questions ──
        c = byCTitle.get("Definition of Machine Learning");
        if (c != null) {
            saveSubjectiveQuestion(c,
                    "In your own words, explain the difference between traditional programming and machine learning.",
                    "traditional programming uses explicit rules while machine learning learns patterns from data",
                    DifficultyLevel.BEGINNER,
                    "In traditional programming, developers write explicit rules. In ML, algorithms learn rules automatically from data — the program improves with experience rather than manual updates.");
            count++;
        }

        c = byCTitle.get("Activation Functions");
        if (c != null) {
            saveSubjectiveQuestion(c,
                    "Why does the sigmoid activation function suffer from the vanishing gradient problem?",
                    "sigmoid outputs saturate near 0 and 1 where the gradient is nearly zero",
                    DifficultyLevel.HARD,
                    "Sigmoid squashes values to (0,1). For very large or small inputs, the output saturates and the derivative approaches 0, making gradients vanishingly small during backpropagation through many layers.");
            count++;
        }

        c = byCTitle.get("Self-Attention and Query-Key-Value");
        if (c != null) {
            saveSubjectiveQuestion(c,
                    "Explain why self-attention has O(n²) computational complexity with respect to sequence length.",
                    "every token computes attention scores with every other token",
                    DifficultyLevel.HARD,
                    "Each of the n tokens must compute a dot product with every other n token to determine attention weights, resulting in n×n attention score computations — hence O(n²) complexity.");
            count++;
        }

        // ── CODING questions ──
        c = byCTitle.get("Simple Linear Regression");
        if (c != null) {
            saveCodingQuestion(c,
                    "Write a Python function that computes the Mean Squared Error (MSE) between two lists of numbers: y_true and y_pred.",
                    "def mse(y_true, y_pred):\n    # Your code here\n    pass",
                    "python",
                    "sum((a - b) ** 2 for a, b in zip(y_true, y_pred)) / len(y_true)",
                    DifficultyLevel.BEGINNER,
                    "MSE = (1/n) * Σ(y_true - y_pred)². Iterate over pairs, square the differences, and divide by the count.");
            count++;
        }

        c = byCTitle.get("DataFrame Fundamentals");
        if (c != null) {
            saveCodingQuestion(c,
                    "Given a Pandas DataFrame `df` with columns 'name', 'age', and 'salary', write code to select all rows where age is greater than 30 and return only the 'name' and 'salary' columns.",
                    "import pandas as pd\n\n# df is already defined\nresult = ___  # Your code here",
                    "python",
                    "df.loc[df['age'] > 30, ['name', 'salary']]",
                    DifficultyLevel.BEGINNER,
                    "Use .loc[] with a boolean condition for row filtering and a list of column names for column selection: df.loc[df['age'] > 30, ['name', 'salary']]");
            count++;
        }

        c = byCTitle.get("The Perceptron Model");
        if (c != null) {
            saveCodingQuestion(c,
                    "Implement a simple perceptron prediction function that takes weights, inputs, and a bias, then returns 1 if the weighted sum plus bias is >= 0, else 0.",
                    "def predict(weights, inputs, bias):\n    # Your code here\n    pass",
                    "python",
                    "1 if sum(w * x for w, x in zip(weights, inputs)) + bias >= 0 else 0",
                    DifficultyLevel.MEDIUM,
                    "A perceptron computes the weighted sum of inputs, adds a bias, and applies a step function: output = 1 if Σ(w·x) + b >= 0, else 0.");
            count++;
        }

        c = byCTitle.get("Next-Token Prediction");
        if (c != null) {
            saveCodingQuestion(c,
                    "Write a Python function that applies temperature scaling to a list of logits and returns the softmax probabilities. Use only basic math (no numpy).",
                    "import math\n\ndef temperature_softmax(logits, temperature=1.0):\n    # Your code here\n    pass",
                    "python",
                    "scaled = [l / temperature for l in logits]; exp_vals = [math.exp(s) for s in scaled]; total = sum(exp_vals); return [e / total for e in exp_vals]",
                    DifficultyLevel.HARD,
                    "Temperature scaling divides each logit by T before applying softmax. Lower T → more deterministic, higher T → more uniform distribution. softmax(x_i) = exp(x_i/T) / Σexp(x_j/T).");
            count++;
        }

        // ── SCENARIO-BASED questions ──
        c = byCTitle.get("Types of Machine Learning");
        if (c != null) {
            saveScenarioQuestion(c,
                    "Which type of machine learning should the team use?",
                    "A retail company has 5 years of transaction data with no labels or categories. They want to discover natural customer segments to personalize marketing campaigns. The team has no pre-defined groups in mind.",
                    List.of("Supervised Learning with classification", "Unsupervised Learning with clustering", "Reinforcement Learning", "Supervised Learning with regression"),
                    "Unsupervised Learning with clustering",
                    DifficultyLevel.MEDIUM,
                    "Since there are no pre-defined labels or categories, unsupervised clustering is ideal — it discovers natural groupings in the data without requiring labeled examples.");
            count++;
        }

        c = byCTitle.get("Activation Functions");
        if (c != null) {
            saveScenarioQuestion(c,
                    "Which activation function should be used in the output layer?",
                    "You are building a neural network for a medical diagnosis system that must classify X-ray images into exactly one of 5 disease categories. The network has 3 hidden layers using ReLU.",
                    List.of("ReLU", "Sigmoid", "Softmax", "Tanh"),
                    "Softmax",
                    DifficultyLevel.MEDIUM,
                    "For multi-class single-label classification (exactly one of 5 categories), Softmax is the correct output activation — it produces a probability distribution across all classes summing to 1.");
            count++;
        }

        c = byCTitle.get("Chain-of-Thought Prompting");
        if (c != null) {
            saveScenarioQuestion(c,
                    "Which prompting technique would be most effective?",
                    "A developer is using an LLM to solve complex math word problems, but the model keeps giving wrong answers when asked directly. The developer has access to 3 solved examples from a textbook.",
                    List.of("Zero-shot prompting", "Few-shot Chain-of-Thought prompting", "Increasing temperature to 1.5", "Using shorter prompts"),
                    "Few-shot Chain-of-Thought prompting",
                    DifficultyLevel.MEDIUM,
                    "Few-shot CoT provides solved examples showing step-by-step reasoning, which teaches the model the reasoning pattern before it attempts the new problem — ideal for complex math.");
            count++;
        }

        c = byCTitle.get("Self-Attention and Query-Key-Value");
        if (c != null) {
            saveScenarioQuestion(c,
                    "What is causing this behavior in the attention mechanism?",
                    "A researcher notices that in their transformer model, the word 'it' in the sentence 'The cat sat on the mat because it was tired' is attending most strongly to 'cat' rather than 'mat'. The model correctly identifies that 'it' refers to the cat.",
                    List.of("The Query of 'it' has high dot-product similarity with the Key of 'cat'",
                            "The model is using positional encoding to select the nearest noun",
                            "The Value vector of 'cat' is the largest",
                            "The model randomly attends to nouns"),
                    "The Query of 'it' has high dot-product similarity with the Key of 'cat'",
                    DifficultyLevel.HARD,
                    "Self-attention computes dot products between Query and Key vectors. When 'it' refers to 'cat', the Query of 'it' will have learned to produce high similarity with the Key of 'cat', creating a strong attention weight.");
            count++;
        }

        return ResponseEntity.ok(Map.of("message", "Questions seeded successfully", "count", count));
    }

    @PostMapping("/seed")
    @Transactional
    public ResponseEntity<Map<String, Object>> seedData() {
        if (courseRepository.count() > 0) {
            // Create admin user if not exists
            if (userRepository.findByEmail("admin@ailearning.com").isEmpty()) {
                User admin = User.builder()
                        .keycloakId("seed-admin-001")
                        .email("admin@ailearning.com")
                        .username("admin")
                        .fullName("Platform Admin")
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .role(UserRole.ADMIN)
                        .totalXp(0L)
                        .build();
                userRepository.save(admin);
            }
            // Fix existing instructor password if missing
            userRepository.findByEmail("dr.sarah.chen@ailearning.com").ifPresent(u -> {
                if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
                    u.setPasswordHash(passwordEncoder.encode("instructor123"));
                    userRepository.save(u);
                }
            });
            // Backfill categories on existing courses
            Map<String, String> categoryMap = Map.of(
                "Machine Learning Fundamentals", "AI & Machine Learning",
                "Deep Learning with Neural Networks", "AI & Machine Learning",
                "Natural Language Processing with Transformers", "AI & Machine Learning",
                "Data Science with Python", "Data Science",
                "Generative AI and Prompt Engineering", "Generative AI"
            );
            long updated = 0;
            for (var entry : categoryMap.entrySet()) {
                for (Course c : courseRepository.findAll()) {
                    if (c.getTitle().equals(entry.getKey()) && c.getCategory() == null) {
                        c.setCategory(entry.getValue());
                        courseRepository.save(c);
                        updated++;
                    }
                }
            }
            return ResponseEntity.ok(Map.of("message", "Data already seeded", "courses", courseRepository.count(), "categoriesUpdated", updated));
        }

        // Create admin user
        if (userRepository.findByEmail("admin@ailearning.com").isEmpty()) {
            User admin = User.builder()
                    .keycloakId("seed-admin-001")
                    .email("admin@ailearning.com")
                    .username("admin")
                    .fullName("Platform Admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .totalXp(0L)
                    .build();
            userRepository.save(admin);
        }

        // Create instructor user
        User instructor = User.builder()
                .keycloakId("seed-instructor-001")
                .email("dr.sarah.chen@ailearning.com")
                .username("dr.sarah.chen")
                .fullName("Dr. Sarah Chen")
                .passwordHash(passwordEncoder.encode("instructor123"))
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
                .category("AI & Machine Learning")
                .createdBy(instructor)
                .published(true)
                .status(CourseStatus.PUBLISHED)
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

        saveQuestion(c1, "What is the key idea behind machine learning?",
                List.of("Writing explicit rules for every scenario", "Letting algorithms discover patterns from data", "Manually classifying each data point", "Using only statistical formulas"),
                "Letting algorithms discover patterns from data", DifficultyLevel.BEGINNER,
                "ML is about providing data and letting algorithms find patterns, rather than writing explicit rules.");
        saveQuestion(c1, "According to Tom Mitchell's definition, what must improve for a program to be considered 'learning'?",
                List.of("Code complexity", "Performance at a task measured by P, with experience E", "Number of rules written", "Processing speed"),
                "Performance at a task measured by P, with experience E", DifficultyLevel.BEGINNER,
                "Mitchell's definition states that learning occurs when performance P at task T improves with experience E.");

        Concept c2 = Concept.builder().topic(t1).title("Types of Machine Learning")
                .definition("The three main paradigms: supervised, unsupervised, and reinforcement learning.")
                .difficultyLevel(DifficultyLevel.BEGINNER).orderIndex(1).build();
        c2 = conceptRepository.save(c2);

        saveLearningUnit(c2, "Three Paradigms of ML", ContentType.TEXT,
                "1. **Supervised Learning** — learns from labeled data (classification, regression)\n2. **Unsupervised Learning** — finds patterns in unlabeled data (clustering, dimensionality reduction)\n3. **Reinforcement Learning** — agent learns by interacting with environment and receiving rewards");

        saveQuestion(c2, "Which type of ML learns from labeled data?",
                List.of("Unsupervised Learning", "Reinforcement Learning", "Supervised Learning", "Semi-supervised Learning"),
                "Supervised Learning", DifficultyLevel.BEGINNER,
                "Supervised learning uses labeled training data (input-output pairs) to learn a mapping function.");
        saveQuestion(c2, "What is an example of unsupervised learning?",
                List.of("Image classification", "Spam detection", "Customer segmentation via clustering", "Self-driving car rewards"),
                "Customer segmentation via clustering", DifficultyLevel.BEGINNER,
                "Clustering is an unsupervised technique that groups similar data points without labeled examples.");

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

        saveQuestion(c3, "What loss function does linear regression typically minimize?",
                List.of("Cross-entropy loss", "Mean Squared Error (MSE)", "Hinge loss", "Log loss"),
                "Mean Squared Error (MSE)", DifficultyLevel.BEGINNER,
                "Linear regression minimizes MSE — the average of squared differences between predicted and actual values.");
        saveQuestion(c3, "In the equation y = mx + b, what does 'm' represent?",
                List.of("The y-intercept", "The slope (weight)", "The input feature", "The prediction error"),
                "The slope (weight)", DifficultyLevel.BEGINNER,
                "In y = mx + b, 'm' is the slope or weight that determines how much x influences y.");

        return course.getTitle();
    }

    private String createDLCourse(User instructor) {
        Course course = Course.builder()
                .title("Deep Learning with Neural Networks")
                .description("From perceptrons to transformers. Master backpropagation, CNNs, RNNs, attention mechanisms, and modern architectures. Hands-on with PyTorch.")
                .shortDescription("From perceptrons to transformers — master deep learning with PyTorch")
                .difficulty(DifficultyLevel.MEDIUM)
                .tags(new String[]{"Deep Learning", "Neural Networks", "PyTorch", "CNN", "Transformers"})
                .category("AI & Machine Learning")
                .createdBy(instructor)
                .published(true)
                .status(CourseStatus.PUBLISHED)
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

        saveQuestion(c1, "What is a key limitation of a single perceptron?",
                List.of("It cannot process numbers", "It can only learn linearly separable patterns", "It requires GPU acceleration", "It needs millions of parameters"),
                "It can only learn linearly separable patterns", DifficultyLevel.BEGINNER,
                "A single perceptron can only classify linearly separable data — it famously cannot learn the XOR function.");
        saveQuestion(c1, "What is the solution to the perceptron's inability to learn XOR?",
                List.of("Use a larger learning rate", "Stack multiple layers (MLP)", "Use unsupervised learning", "Use a different activation function alone"),
                "Stack multiple layers (MLP)", DifficultyLevel.MEDIUM,
                "By stacking perceptrons into multiple layers (Multi-Layer Perceptron), the network can learn non-linear decision boundaries.");

        Concept c2 = Concept.builder().topic(t1).title("Activation Functions")
                .definition("Non-linear functions enabling neural networks to learn complex patterns.")
                .difficultyLevel(DifficultyLevel.MEDIUM).orderIndex(1).build();
        c2 = conceptRepository.save(c2);

        saveLearningUnit(c2, "Activation Functions Deep Dive", ContentType.TEXT,
                "ReLU: f(x) = max(0, x) — Default for hidden layers\nSigmoid: output (0,1) — Binary classification\nTanh: output (-1,1) — Zero-centered\nSoftmax: probability distribution — Multi-class output");

        saveQuestion(c2, "Which activation function is the default choice for hidden layers in modern neural networks?",
                List.of("Sigmoid", "Tanh", "ReLU", "Softmax"),
                "ReLU", DifficultyLevel.MEDIUM,
                "ReLU (Rectified Linear Unit) is the default for hidden layers because it avoids the vanishing gradient problem and is computationally efficient.");
        saveQuestion(c2, "Which activation function outputs a probability distribution over multiple classes?",
                List.of("ReLU", "Sigmoid", "Tanh", "Softmax"),
                "Softmax", DifficultyLevel.MEDIUM,
                "Softmax converts a vector of values into a probability distribution, making it ideal for multi-class classification output layers.");

        return course.getTitle();
    }

    private String createNLPCourse(User instructor) {
        Course course = Course.builder()
                .title("Natural Language Processing with Transformers")
                .description("From text preprocessing to GPT. Cover tokenization, embeddings, attention mechanisms, transformers, BERT, GPT, and fine-tuning large language models.")
                .shortDescription("Master NLP from tokenization to fine-tuning GPT and BERT")
                .difficulty(DifficultyLevel.HARD)
                .tags(new String[]{"NLP", "Transformers", "BERT", "GPT", "LLM"})
                .category("AI & Machine Learning")
                .createdBy(instructor)
                .published(true)
                .status(CourseStatus.PUBLISHED)
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

        saveQuestion(c1, "In self-attention, what does the Query vector represent?",
                List.of("What information a token provides", "What a token is looking for", "The importance weight of a token", "The position of a token"),
                "What a token is looking for", DifficultyLevel.HARD,
                "The Query (Q) vector represents what a token is searching for — it's compared against Key vectors of all other tokens to compute attention weights.");
        saveQuestion(c1, "Why is the attention score divided by sqrt(d_k)?",
                List.of("To make computation faster", "To prevent dot products from growing too large, stabilizing gradients", "To normalize the output between 0 and 1", "To reduce memory usage"),
                "To prevent dot products from growing too large, stabilizing gradients", DifficultyLevel.HARD,
                "Dividing by sqrt(d_k) prevents the dot products from becoming very large in high dimensions, which would push softmax into regions with tiny gradients.");

        return course.getTitle();
    }

    private String createDSCourse(User instructor) {
        Course course = Course.builder()
                .title("Data Science with Python")
                .description("Complete data science toolkit. Master NumPy, Pandas, Matplotlib, Seaborn, statistical analysis, EDA, hypothesis testing, and storytelling with data.")
                .shortDescription("The complete Python data science toolkit — from EDA to storytelling")
                .difficulty(DifficultyLevel.BEGINNER)
                .tags(new String[]{"Python", "Pandas", "NumPy", "Data Visualization", "Statistics"})
                .category("Data Science")
                .createdBy(instructor)
                .published(true)
                .status(CourseStatus.PUBLISHED)
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

        saveQuestion(c1, "Which Pandas method is used for label-based indexing?",
                List.of(".iloc[]", ".loc[]", ".ix[]", ".at[]"),
                ".loc[]", DifficultyLevel.BEGINNER,
                ".loc[] is used for label-based indexing (by row/column labels), while .iloc[] is for integer-position based indexing.");
        saveQuestion(c1, "What does the groupby() method in Pandas do?",
                List.of("Sorts the DataFrame", "Splits data into groups based on criteria for aggregation", "Merges two DataFrames", "Filters null values"),
                "Splits data into groups based on criteria for aggregation", DifficultyLevel.BEGINNER,
                "groupby() splits the DataFrame into groups, applies a function (like mean, sum), and combines results — the split-apply-combine pattern.");

        return course.getTitle();
    }

    private String createGenAICourse(User instructor) {
        Course course = Course.builder()
                .title("Generative AI and Prompt Engineering")
                .description("Understand and harness generative AI. Learn how LLMs work, master prompt engineering, build RAG systems, use function calling, and create AI agents.")
                .shortDescription("Master LLMs, prompt engineering, RAG, and AI agents")
                .difficulty(DifficultyLevel.MEDIUM)
                .tags(new String[]{"Generative AI", "LLM", "Prompt Engineering", "RAG", "AI Agents"})
                .category("Generative AI")
                .createdBy(instructor)
                .published(true)
                .status(CourseStatus.PUBLISHED)
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

        saveQuestion(c1, "What is the fundamental mechanism by which LLMs generate text?",
                List.of("Rule-based grammar parsing", "Next-token prediction", "Template filling", "Database lookup"),
                "Next-token prediction", DifficultyLevel.MEDIUM,
                "LLMs generate text by predicting the most likely next token given the preceding context, one token at a time.");
        saveQuestion(c1, "What does the temperature parameter control in LLM text generation?",
                List.of("The speed of generation", "The randomness/creativity of output", "The maximum length of output", "The language of output"),
                "The randomness/creativity of output", DifficultyLevel.MEDIUM,
                "Temperature controls randomness: 0 makes output deterministic (greedy), while higher values increase diversity by flattening the probability distribution.");

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

        saveQuestion(c2, "What is the simplest way to trigger chain-of-thought reasoning in an LLM?",
                List.of("Provide many examples", "Add 'Let's think step by step' to the prompt", "Increase the temperature to 1.0", "Use a larger model"),
                "Add 'Let's think step by step' to the prompt", DifficultyLevel.MEDIUM,
                "Zero-shot CoT can be triggered by simply appending 'Let's think step by step' — this encourages the model to show its reasoning process.");
        saveQuestion(c2, "Which type of tasks benefit most from chain-of-thought prompting?",
                List.of("Simple factual recall", "Text translation", "Math, logic, and multi-step reasoning", "Image generation"),
                "Math, logic, and multi-step reasoning", DifficultyLevel.MEDIUM,
                "CoT is most beneficial for tasks requiring sequential reasoning — math problems, logical deductions, and multi-step problem solving.");

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

    private void saveQuestion(Concept concept, String questionText, List<String> options,
                              String correctAnswer, DifficultyLevel difficulty, String explanation) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("options", options);
        metadata.put("correctAnswer", correctAnswer);
        Question question = Question.builder()
                .concept(concept)
                .type(QuestionType.MCQ)
                .questionText(questionText)
                .metadata(metadata)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .explanation(explanation)
                .aiGenerated(false)
                .build();
        questionRepository.save(question);
    }

    private void saveCodingQuestion(Concept concept, String questionText, String starterCode,
                                     String language, String correctAnswer, DifficultyLevel difficulty, String explanation) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("starterCode", starterCode);
        metadata.put("language", language);
        Question question = Question.builder()
                .concept(concept)
                .type(QuestionType.CODING)
                .questionText(questionText)
                .metadata(metadata)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .explanation(explanation)
                .aiGenerated(false)
                .build();
        questionRepository.save(question);
    }

    private void saveSubjectiveQuestion(Concept concept, String questionText, String correctAnswer,
                                         DifficultyLevel difficulty, String explanation) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("answerFormat", "text");
        Question question = Question.builder()
                .concept(concept)
                .type(QuestionType.SUBJECTIVE)
                .questionText(questionText)
                .metadata(metadata)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .explanation(explanation)
                .aiGenerated(false)
                .build();
        questionRepository.save(question);
    }

    private void saveScenarioQuestion(Concept concept, String questionText, String scenario,
                                       List<String> options, String correctAnswer,
                                       DifficultyLevel difficulty, String explanation) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("scenario", scenario);
        metadata.put("options", options);
        Question question = Question.builder()
                .concept(concept)
                .type(QuestionType.SCENARIO_BASED)
                .questionText(questionText)
                .metadata(metadata)
                .correctAnswer(correctAnswer)
                .difficulty(difficulty)
                .explanation(explanation)
                .aiGenerated(false)
                .build();
        questionRepository.save(question);
    }
}
