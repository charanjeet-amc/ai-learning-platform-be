-- V2__seed_courses.sql
-- Seed data: instructor user + 5 courses with modules, topics, concepts, and learning units

-- ==================== INSTRUCTOR USER ====================
INSERT INTO users (id, keycloak_id, email, username, display_name, role, subscription_tier, total_xp, bio)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'seed-instructor-001',
    'dr.sarah.chen@ailearning.com',
    'dr.sarah.chen',
    'Dr. Sarah Chen',
    'INSTRUCTOR',
    'PRO',
    50000,
    'PhD in Computer Science from Stanford. 15+ years teaching AI/ML. Former research scientist at Google DeepMind.'
);

-- ==================== COURSE 1: Machine Learning Fundamentals ====================
INSERT INTO courses (id, title, slug, description, short_description, difficulty, category, tags, instructor_id, published, rating, enrollment_count, estimated_hours)
VALUES (
    'c0000000-0000-0000-0000-000000000001',
    'Machine Learning Fundamentals',
    'machine-learning-fundamentals',
    'Master the core principles of machine learning from scratch. This course covers supervised and unsupervised learning, model evaluation, feature engineering, and real-world applications. Build a strong foundation with hands-on coding exercises and projects using Python and scikit-learn.',
    'Learn ML from zero to hero with hands-on Python projects',
    'BEGINNER',
    'Artificial Intelligence',
    ARRAY['Machine Learning', 'Python', 'scikit-learn', 'Data Science', 'AI'],
    'a0000000-0000-0000-0000-000000000001',
    TRUE, 4.8, 12540, 40
);

-- Module 1.1: Introduction to ML
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0001-000000000001',
    'c0000000-0000-0000-0000-000000000001',
    'Introduction to Machine Learning',
    'Understand what machine learning is, its types, and where it is used in the real world.',
    0,
    ARRAY['Define machine learning and its key paradigms', 'Distinguish supervised, unsupervised, and reinforcement learning', 'Identify real-world ML applications'],
    120
);

-- Topic 1.1.1: What is ML?
INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0001-0001-000000000001',
    'm0000000-0000-0000-0001-000000000001',
    'What is Machine Learning?',
    'Core definition, history, and the ML landscape.',
    0,
    ARRAY['ML basics', 'history']
);

-- Concept 1.1.1.1: Definition of ML
INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0001-0001-0001-000000000001',
    't0000000-0000-0001-0001-000000000001',
    'Definition of Machine Learning',
    'Machine learning is a subset of artificial intelligence that enables systems to learn and improve from experience without being explicitly programmed. Arthur Samuel defined it as the field of study that gives computers the ability to learn without being explicitly programmed.',
    'BEGINNER', 0, 0.8, 15
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0001-0001-0001-000000000001',
    'x0000000-0001-0001-0001-000000000001',
    'What is Machine Learning?',
    'TEXT',
    '{"body": "Machine learning (ML) is a branch of artificial intelligence (AI) focused on building systems that learn from data. Unlike traditional programming where you write explicit rules, ML algorithms discover patterns from data and make decisions with minimal human intervention.\n\n**Key Idea:** Instead of coding rules, you provide data and let the algorithm find the rules.\n\n**Historical Context:**\n- 1959: Arthur Samuel coined the term while at IBM\n- 1997: Tom Mitchell formalized the definition\n- 2012: Deep learning revolution began with AlexNet\n\n**Tom Mitchell''s Formal Definition:**\nA computer program is said to learn from experience E with respect to some task T and performance measure P, if its performance at task T, as measured by P, improves with experience E.", "summary": "ML enables computers to learn patterns from data without explicit programming."}',
    0, 10
);

-- Concept 1.1.1.2: Types of ML
INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0001-0001-0001-000000000002',
    't0000000-0000-0001-0001-000000000001',
    'Types of Machine Learning',
    'The three main paradigms: supervised learning, unsupervised learning, and reinforcement learning.',
    'BEGINNER', 1, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0001-0001-0001-000000000002',
    'x0000000-0001-0001-0001-000000000002',
    'Three Paradigms of ML',
    'TEXT',
    '{"body": "Machine learning is broadly categorized into three paradigms:\n\n## 1. Supervised Learning\nThe algorithm learns from **labeled data** — input-output pairs.\n- **Classification:** Predict a category (spam vs not spam)\n- **Regression:** Predict a continuous value (house price)\n- Examples: Linear Regression, Decision Trees, SVMs\n\n## 2. Unsupervised Learning\nThe algorithm finds patterns in **unlabeled data**.\n- **Clustering:** Group similar items (customer segments)\n- **Dimensionality Reduction:** Compress features (PCA)\n- Examples: K-Means, DBSCAN, Autoencoders\n\n## 3. Reinforcement Learning\nAn agent learns by **interacting with an environment** and receiving rewards.\n- **Goal:** Maximize cumulative reward over time\n- Examples: Game playing (AlphaGo), Robotics, Autonomous driving", "summary": "ML has three paradigms: supervised (labeled data), unsupervised (unlabeled data), and reinforcement (reward-based)."}',
    0, 15
);

-- Topic 1.1.2: The ML Pipeline
INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0001-0001-000000000002',
    'm0000000-0000-0000-0001-000000000001',
    'The Machine Learning Pipeline',
    'End-to-end workflow from data to deployment.',
    1,
    ARRAY['pipeline', 'workflow']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0001-0001-0002-000000000001',
    't0000000-0000-0001-0001-000000000002',
    'Data Collection and Preprocessing',
    'How to gather, clean, and prepare data for ML models.',
    'BEGINNER', 0, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0001-0001-0002-000000000001',
    'x0000000-0001-0001-0002-000000000001',
    'Data Preprocessing Essentials',
    'TEXT',
    '{"body": "Data is the fuel of machine learning. The quality of your data directly determines the quality of your model.\n\n## Data Collection\n- Public datasets (Kaggle, UCI Repository)\n- Web scraping, APIs, databases\n- Surveys and manual collection\n\n## Data Cleaning\n1. **Handle missing values:** Imputation (mean, median, mode) or deletion\n2. **Remove duplicates:** Exact and near-duplicate detection\n3. **Fix inconsistencies:** Standardize formats, correct typos\n\n## Feature Engineering\n- **Scaling:** Min-Max normalization, StandardScaler\n- **Encoding:** One-Hot Encoding for categorical variables\n- **Feature Selection:** Remove irrelevant or redundant features\n\n## Train-Test Split\nAlways split data BEFORE any preprocessing to avoid data leakage.", "summary": "Data preprocessing involves collection, cleaning, feature engineering, and proper train-test splitting."}',
    0, 15
);

-- Module 1.2: Supervised Learning
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0001-000000000002',
    'c0000000-0000-0000-0000-000000000001',
    'Supervised Learning Algorithms',
    'Deep dive into classification and regression algorithms.',
    1,
    ARRAY['Implement linear and logistic regression', 'Build decision trees and random forests', 'Understand model evaluation metrics'],
    180
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0001-0002-000000000001',
    'm0000000-0000-0000-0001-000000000002',
    'Linear Regression',
    'The foundation of predictive modeling.',
    0,
    ARRAY['regression', 'linear models']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0001-0002-0001-000000000001',
    't0000000-0000-0001-0002-000000000001',
    'Simple Linear Regression',
    'Modeling the relationship between a single feature and a target variable using a straight line.',
    'BEGINNER', 0, 0.8, 25
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0001-0002-0001-000000000001',
    'x0000000-0001-0002-0001-000000000001',
    'Understanding Linear Regression',
    'TEXT',
    '{"body": "Linear Regression fits a straight line through data points to predict a continuous output.\n\n## The Equation\n$$y = mx + b$$\nWhere:\n- **y** = predicted value\n- **m** = slope (weight/coefficient)\n- **x** = input feature\n- **b** = intercept (bias)\n\n## Cost Function\nWe minimize the **Mean Squared Error (MSE)**:\n$$MSE = \\frac{1}{n} \\sum_{i=1}^{n} (y_i - \\hat{y}_i)^2$$\n\n## Gradient Descent\nIteratively adjust weights to minimize cost:\n1. Initialize weights randomly\n2. Calculate predictions\n3. Compute gradients\n4. Update weights: $w = w - \\alpha \\cdot \\nabla J$\n5. Repeat until convergence\n\n## Python Example\n```python\nfrom sklearn.linear_model import LinearRegression\nmodel = LinearRegression()\nmodel.fit(X_train, y_train)\npredictions = model.predict(X_test)\n```", "summary": "Linear regression models y = mx + b, minimizing MSE via gradient descent."}',
    0, 20
);

-- Module 1.3: Model Evaluation
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0001-000000000003',
    'c0000000-0000-0000-0000-000000000001',
    'Model Evaluation and Validation',
    'Learn to measure model performance and avoid common pitfalls.',
    2,
    ARRAY['Calculate accuracy, precision, recall, and F1', 'Use cross-validation techniques', 'Diagnose bias-variance tradeoff'],
    150
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0001-0003-000000000001',
    'm0000000-0000-0000-0001-000000000003',
    'Classification Metrics',
    'Precision, recall, F1-score, and confusion matrices.',
    0,
    ARRAY['metrics', 'evaluation']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0001-0003-0001-000000000001',
    't0000000-0000-0001-0003-000000000001',
    'Confusion Matrix and Derived Metrics',
    'Understanding true positives, false positives, precision, recall, and F1-score.',
    'INTERMEDIATE', 0, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0001-0003-0001-000000000001',
    'x0000000-0001-0003-0001-000000000001',
    'Mastering the Confusion Matrix',
    'TEXT',
    '{"body": "The confusion matrix is the foundation for classification metrics.\n\n## The Matrix\n|  | Predicted Positive | Predicted Negative |\n|--|---|---|\n| **Actual Positive** | True Positive (TP) | False Negative (FN) |\n| **Actual Negative** | False Positive (FP) | True Negative (TN) |\n\n## Key Metrics\n- **Accuracy** = (TP + TN) / Total\n- **Precision** = TP / (TP + FP) — \"Of all positive predictions, how many were correct?\"\n- **Recall** = TP / (TP + FN) — \"Of all actual positives, how many did we catch?\"\n- **F1-Score** = 2 × (Precision × Recall) / (Precision + Recall)\n\n## When to Use What\n- **High Precision needed:** Spam filtering (don''t lose real emails)\n- **High Recall needed:** Cancer detection (don''t miss any cases)\n- **F1-Score:** When you need balance between precision and recall", "summary": "The confusion matrix gives TP/FP/TN/FN, from which we derive precision, recall, and F1-score."}',
    0, 15
);


-- ==================== COURSE 2: Deep Learning with Neural Networks ====================
INSERT INTO courses (id, title, slug, description, short_description, difficulty, category, tags, instructor_id, published, rating, enrollment_count, estimated_hours)
VALUES (
    'c0000000-0000-0000-0000-000000000002',
    'Deep Learning with Neural Networks',
    'deep-learning-neural-networks',
    'Dive deep into neural networks, from perceptrons to transformers. Master backpropagation, CNNs, RNNs, LSTMs, attention mechanisms, and modern architectures like GPT and BERT. Includes hands-on projects with PyTorch.',
    'From perceptrons to transformers — master deep learning with PyTorch',
    'INTERMEDIATE',
    'Artificial Intelligence',
    ARRAY['Deep Learning', 'Neural Networks', 'PyTorch', 'CNN', 'Transformers'],
    'a0000000-0000-0000-0000-000000000001',
    TRUE, 4.9, 8930, 60
);

-- Module 2.1: Neural Network Foundations
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0002-000000000001',
    'c0000000-0000-0000-0000-000000000002',
    'Neural Network Foundations',
    'Build intuition for how neural networks learn from data.',
    0,
    ARRAY['Explain the perceptron model', 'Implement forward and backward propagation', 'Choose appropriate activation functions'],
    180
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0002-0001-000000000001',
    'm0000000-0000-0000-0002-000000000001',
    'The Perceptron and Activation Functions',
    'Building blocks of neural networks.',
    0,
    ARRAY['perceptron', 'activation functions']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0002-0001-0001-000000000001',
    't0000000-0000-0002-0001-000000000001',
    'The Perceptron Model',
    'The simplest neural network: a single neuron that makes binary decisions.',
    'BEGINNER', 0, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0002-0001-0001-000000000001',
    'x0000000-0002-0001-0001-000000000001',
    'Understanding the Perceptron',
    'TEXT',
    '{"body": "The perceptron, invented by Frank Rosenblatt in 1958, is the simplest form of a neural network.\n\n## How It Works\n1. Takes multiple inputs: $x_1, x_2, ..., x_n$\n2. Multiplies each by a weight: $w_1, w_2, ..., w_n$\n3. Sums them up: $z = \\sum w_i x_i + b$\n4. Applies a step function: output = 1 if z > 0, else 0\n\n## The Learning Rule\nFor each misclassified example:\n$$w_i = w_i + \\alpha \\cdot (y - \\hat{y}) \\cdot x_i$$\n\n## Limitations\n- Can only learn **linearly separable** patterns\n- Cannot learn XOR (this led to the first AI winter)\n- Solution: Stack multiple layers → Multi-Layer Perceptron (MLP)\n\n## PyTorch Implementation\n```python\nimport torch.nn as nn\n\nclass Perceptron(nn.Module):\n    def __init__(self, input_size):\n        super().__init__()\n        self.linear = nn.Linear(input_size, 1)\n    \n    def forward(self, x):\n        return torch.sigmoid(self.linear(x))\n```", "summary": "The perceptron is a single neuron that computes a weighted sum and applies a threshold function."}',
    0, 15
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0002-0001-0001-000000000002',
    't0000000-0000-0002-0001-000000000001',
    'Activation Functions',
    'Non-linear functions that enable neural networks to learn complex patterns.',
    'INTERMEDIATE', 1, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0002-0001-0001-000000000002',
    'x0000000-0002-0001-0001-000000000002',
    'Activation Functions Deep Dive',
    'TEXT',
    '{"body": "Activation functions introduce non-linearity, allowing neural networks to learn complex mappings.\n\n## Common Activation Functions\n\n### ReLU (Rectified Linear Unit)\n$$f(x) = \\max(0, x)$$\n- **Pros:** Fast, avoids vanishing gradient\n- **Cons:** Dying ReLU problem\n- **Use:** Default for hidden layers\n\n### Sigmoid\n$$f(x) = \\frac{1}{1 + e^{-x}}$$\n- **Output:** (0, 1)\n- **Use:** Binary classification output layer\n\n### Tanh\n$$f(x) = \\frac{e^x - e^{-x}}{e^x + e^{-x}}$$\n- **Output:** (-1, 1)\n- **Use:** When zero-centered output is needed\n\n### Softmax\n$$f(x_i) = \\frac{e^{x_i}}{\\sum_j e^{x_j}}$$\n- **Output:** Probability distribution\n- **Use:** Multi-class classification output layer\n\n## Rule of Thumb\n- Hidden layers: **ReLU** (or variants like LeakyReLU)\n- Output layer: **Sigmoid** (binary), **Softmax** (multi-class), **None** (regression)", "summary": "Activation functions (ReLU, Sigmoid, Tanh, Softmax) add non-linearity enabling complex pattern learning."}',
    0, 15
);

-- Module 2.2: Backpropagation
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0002-000000000002',
    'c0000000-0000-0000-0000-000000000002',
    'Backpropagation and Optimization',
    'How neural networks learn through gradient-based optimization.',
    1,
    ARRAY['Derive backpropagation using the chain rule', 'Compare SGD, Adam, and RMSProp optimizers', 'Implement training loops in PyTorch'],
    200
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0002-0002-000000000001',
    'm0000000-0000-0000-0002-000000000002',
    'Backpropagation Algorithm',
    'The engine that powers neural network learning.',
    0,
    ARRAY['backpropagation', 'chain rule', 'gradients']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0002-0002-0001-000000000001',
    't0000000-0000-0002-0002-000000000001',
    'The Chain Rule and Backpropagation',
    'How gradients flow backward through the network to update weights.',
    'INTERMEDIATE', 0, 0.8, 30
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0002-0002-0001-000000000001',
    'x0000000-0002-0002-0001-000000000001',
    'Backpropagation Explained',
    'TEXT',
    '{"body": "Backpropagation is the algorithm that makes neural network training possible.\n\n## The Core Idea\n1. **Forward Pass:** Compute predictions layer by layer\n2. **Compute Loss:** Compare predictions to actual labels\n3. **Backward Pass:** Compute gradients of loss w.r.t. each weight using the chain rule\n4. **Update Weights:** Adjust weights in the direction that reduces loss\n\n## The Chain Rule\nFor a composition $f(g(x))$:\n$$\\frac{df}{dx} = \\frac{df}{dg} \\cdot \\frac{dg}{dx}$$\n\nIn a neural network with layers $L_1 \\rightarrow L_2 \\rightarrow L_3$:\n$$\\frac{\\partial Loss}{\\partial w_1} = \\frac{\\partial Loss}{\\partial L_3} \\cdot \\frac{\\partial L_3}{\\partial L_2} \\cdot \\frac{\\partial L_2}{\\partial w_1}$$\n\n## PyTorch Autograd\n```python\nloss = criterion(output, target)\nloss.backward()      # Computes all gradients\noptimizer.step()     # Updates all weights\noptimizer.zero_grad() # Resets gradient buffers\n```\n\nPyTorch''s autograd handles backpropagation automatically through its computational graph.", "summary": "Backpropagation uses the chain rule to compute gradients and update weights layer by layer."}',
    0, 25
);

-- Module 2.3: Convolutional Neural Networks
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0002-000000000003',
    'c0000000-0000-0000-0000-000000000002',
    'Convolutional Neural Networks',
    'Master CNNs for image recognition and computer vision tasks.',
    2,
    ARRAY['Explain convolution and pooling operations', 'Build CNN architectures for image classification', 'Apply transfer learning with pretrained models'],
    240
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0002-0003-000000000001',
    'm0000000-0000-0000-0002-000000000003',
    'CNN Architecture',
    'Convolution layers, pooling, and feature maps.',
    0,
    ARRAY['CNN', 'convolution', 'pooling']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0002-0003-0001-000000000001',
    't0000000-0000-0002-0003-000000000001',
    'Convolution Operations',
    'How convolutional layers detect features in images using learnable filters.',
    'INTERMEDIATE', 0, 0.8, 25
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0002-0003-0001-000000000001',
    'x0000000-0002-0003-0001-000000000001',
    'Understanding Convolutions',
    'TEXT',
    '{"body": "Convolutional layers are the building blocks of CNNs, designed to automatically detect spatial features.\n\n## How Convolution Works\n1. A small **filter/kernel** (e.g., 3×3) slides across the input image\n2. At each position, compute element-wise multiplication and sum\n3. This produces a **feature map** that highlights detected patterns\n\n## Key Parameters\n- **Kernel Size:** Typically 3×3 or 5×5\n- **Stride:** How many pixels the filter moves (default: 1)\n- **Padding:** Adding zeros around the border to preserve spatial dimensions\n- **Number of Filters:** Each filter learns a different feature\n\n## Feature Hierarchy\n- **Layer 1:** Edges, corners, simple textures\n- **Layer 2:** Parts of objects (eyes, wheels)\n- **Layer 3+:** Complete objects and complex patterns\n\n## PyTorch CNN Layer\n```python\nimport torch.nn as nn\n\nconv_layer = nn.Conv2d(\n    in_channels=3,    # RGB input\n    out_channels=32,  # 32 filters\n    kernel_size=3,    # 3x3 filter\n    padding=1         # Same padding\n)\n```", "summary": "Convolutional layers use learnable filters that slide over images to detect hierarchical features."}',
    0, 20
);


-- ==================== COURSE 3: Natural Language Processing ====================
INSERT INTO courses (id, title, slug, description, short_description, difficulty, category, tags, instructor_id, published, rating, enrollment_count, estimated_hours)
VALUES (
    'c0000000-0000-0000-0000-000000000003',
    'Natural Language Processing with Transformers',
    'nlp-transformers',
    'From text preprocessing to GPT — master NLP end to end. Cover tokenization, embeddings, attention mechanisms, transformers, BERT, GPT, and fine-tuning large language models. Build chatbots, sentiment analyzers, and text generators.',
    'Master NLP from tokenization to fine-tuning GPT and BERT',
    'ADVANCED',
    'Artificial Intelligence',
    ARRAY['NLP', 'Transformers', 'BERT', 'GPT', 'LLM', 'Hugging Face'],
    'a0000000-0000-0000-0000-000000000001',
    TRUE, 4.7, 6720, 50
);

-- Module 3.1: Text Preprocessing
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0003-000000000001',
    'c0000000-0000-0000-0000-000000000003',
    'Text Preprocessing and Representation',
    'Transform raw text into numerical representations for ML models.',
    0,
    ARRAY['Implement tokenization and stemming', 'Create Bag-of-Words and TF-IDF representations', 'Understand word embeddings (Word2Vec, GloVe)'],
    150
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0003-0001-000000000001',
    'm0000000-0000-0000-0003-000000000001',
    'Tokenization and Text Cleaning',
    'Preparing raw text for NLP pipelines.',
    0,
    ARRAY['tokenization', 'text cleaning', 'preprocessing']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0003-0001-0001-000000000001',
    't0000000-0000-0003-0001-000000000001',
    'Tokenization Strategies',
    'Breaking text into tokens — words, subwords, or characters.',
    'INTERMEDIATE', 0, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0003-0001-0001-000000000001',
    'x0000000-0003-0001-0001-000000000001',
    'Tokenization Deep Dive',
    'TEXT',
    '{"body": "Tokenization is the first step in any NLP pipeline — splitting text into meaningful units.\n\n## Types of Tokenization\n\n### Word Tokenization\n```python\n\"I love NLP\" → [\"I\", \"love\", \"NLP\"]\n```\n- Simple but has vocabulary explosion problems\n- Cannot handle out-of-vocabulary (OOV) words\n\n### Subword Tokenization (BPE)\n```python\n\"unhappiness\" → [\"un\", \"happiness\"]\n```\n- **Byte-Pair Encoding (BPE):** Used by GPT\n- **WordPiece:** Used by BERT\n- **SentencePiece:** Used by T5, ALBERT\n- Handles rare words and morphology\n\n### Character Tokenization\n```python\n\"hello\" → [\"h\", \"e\", \"l\", \"l\", \"o\"]\n```\n- No OOV problem but very long sequences\n\n## Modern Approach: Hugging Face Tokenizers\n```python\nfrom transformers import AutoTokenizer\ntokenizer = AutoTokenizer.from_pretrained(\"bert-base-uncased\")\ntokens = tokenizer(\"I love transformers!\")\nprint(tokens[\"input_ids\"])  # [101, 1045, 2293, 19081, 999, 102]\n```", "summary": "Tokenization splits text into tokens; modern NLP uses subword methods like BPE (GPT) and WordPiece (BERT)."}',
    0, 15
);

-- Module 3.2: Attention and Transformers
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0003-000000000002',
    'c0000000-0000-0000-0000-000000000003',
    'Attention Mechanisms and Transformers',
    'The architecture that revolutionized NLP and AI.',
    1,
    ARRAY['Explain self-attention and multi-head attention', 'Implement a transformer encoder from scratch', 'Compare encoder-only, decoder-only, and encoder-decoder architectures'],
    240
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0003-0002-000000000001',
    'm0000000-0000-0000-0003-000000000002',
    'Self-Attention Mechanism',
    'The core innovation behind transformers.',
    0,
    ARRAY['attention', 'self-attention', 'query key value']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0003-0002-0001-000000000001',
    't0000000-0000-0003-0002-000000000001',
    'Self-Attention and Query-Key-Value',
    'How transformers weigh the importance of different parts of the input sequence.',
    'ADVANCED', 0, 0.8, 30
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0003-0002-0001-000000000001',
    'x0000000-0003-0002-0001-000000000001',
    'Self-Attention Explained',
    'TEXT',
    '{"body": "Self-attention allows each token to attend to every other token in the sequence.\n\n## The Query-Key-Value Framework\nFor each token, we compute three vectors:\n- **Query (Q):** What am I looking for?\n- **Key (K):** What do I contain?\n- **Value (V):** What information do I provide?\n\n## Scaled Dot-Product Attention\n$$\\text{Attention}(Q, K, V) = \\text{softmax}\\left(\\frac{QK^T}{\\sqrt{d_k}}\\right) V$$\n\n### Step by Step:\n1. Compute attention scores: $QK^T$ (dot product of queries and keys)\n2. Scale by $\\sqrt{d_k}$ to prevent vanishing gradients in softmax\n3. Apply softmax to get attention weights (probabilities)\n4. Multiply by V to get the weighted output\n\n## Multi-Head Attention\nInstead of one attention function, use multiple heads:\n$$\\text{MultiHead}(Q, K, V) = \\text{Concat}(head_1, ..., head_h) W^O$$\n\nEach head can attend to different aspects (syntax, semantics, coreference).\n\n## Why Self-Attention?\n- **Parallelizable:** Unlike RNNs, all positions computed simultaneously\n- **Long-range dependencies:** Direct connection between any two positions\n- **Interpretable:** Attention weights show what the model focuses on", "summary": "Self-attention uses Query-Key-Value to let each token weigh every other token; multi-head attention captures different relationship types."}',
    0, 25
);


-- ==================== COURSE 4: Data Science with Python ====================
INSERT INTO courses (id, title, slug, description, short_description, difficulty, category, tags, instructor_id, published, rating, enrollment_count, estimated_hours)
VALUES (
    'c0000000-0000-0000-0000-000000000004',
    'Data Science with Python',
    'data-science-python',
    'Complete data science toolkit with Python. Master NumPy, Pandas, Matplotlib, Seaborn, and statistical analysis. Learn exploratory data analysis (EDA), hypothesis testing, A/B testing, and storytelling with data through real-world case studies.',
    'The complete Python data science toolkit — from EDA to storytelling',
    'BEGINNER',
    'Data Science',
    ARRAY['Python', 'Pandas', 'NumPy', 'Data Visualization', 'Statistics', 'EDA'],
    'a0000000-0000-0000-0000-000000000001',
    TRUE, 4.6, 18200, 35
);

-- Module 4.1: Python for Data Science
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0004-000000000001',
    'c0000000-0000-0000-0000-000000000004',
    'NumPy and Pandas Essentials',
    'Master the two foundational libraries for data manipulation in Python.',
    0,
    ARRAY['Create and manipulate NumPy arrays', 'Load, filter, and transform data with Pandas', 'Handle missing data and perform groupby operations'],
    180
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0004-0001-000000000001',
    'm0000000-0000-0000-0004-000000000001',
    'Pandas DataFrame Operations',
    'The Swiss Army knife of data manipulation.',
    0,
    ARRAY['pandas', 'dataframe', 'data manipulation']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0004-0001-0001-000000000001',
    't0000000-0000-0004-0001-000000000001',
    'DataFrame Fundamentals',
    'Creating, indexing, filtering, and transforming DataFrames.',
    'BEGINNER', 0, 0.8, 25
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0004-0001-0001-000000000001',
    'x0000000-0004-0001-0001-000000000001',
    'Mastering Pandas DataFrames',
    'TEXT',
    '{"body": "Pandas DataFrame is the primary data structure for data science in Python.\n\n## Creating a DataFrame\n```python\nimport pandas as pd\n\ndf = pd.DataFrame({\n    ''name'': [''Alice'', ''Bob'', ''Charlie''],\n    ''age'': [25, 30, 35],\n    ''salary'': [50000, 60000, 70000]\n})\n```\n\n## Essential Operations\n\n### Selecting Columns\n```python\ndf[''name'']           # Single column (Series)\ndf[[''name'', ''age'']]  # Multiple columns (DataFrame)\n```\n\n### Filtering Rows\n```python\ndf[df[''age''] > 28]               # Boolean indexing\ndf.query(''age > 28 and salary > 55000'')  # Query syntax\n```\n\n### Handling Missing Data\n```python\ndf.isnull().sum()        # Count missing per column\ndf.fillna(0)             # Fill with value\ndf.dropna()              # Drop rows with missing values\n```\n\n### GroupBy\n```python\ndf.groupby(''department'')[''salary''].mean()\ndf.groupby(''department'').agg({''salary'': [''mean'', ''max''], ''age'': ''count''})\n```\n\n## Pro Tips\n- Use `.loc[]` for label-based indexing\n- Use `.iloc[]` for position-based indexing\n- Chain operations with `.pipe()` for readability", "summary": "Pandas DataFrames enable powerful data manipulation: selection, filtering, missing data handling, and groupby aggregations."}',
    0, 20
);

-- Module 4.2: Data Visualization
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0004-000000000002',
    'c0000000-0000-0000-0000-000000000004',
    'Data Visualization with Matplotlib and Seaborn',
    'Create compelling visualizations that tell stories with data.',
    1,
    ARRAY['Create publication-quality plots with Matplotlib', 'Build statistical visualizations with Seaborn', 'Choose the right chart type for your data'],
    150
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0004-0002-000000000001',
    'm0000000-0000-0000-0004-000000000002',
    'Statistical Plotting with Seaborn',
    'Beautiful statistical visualizations made easy.',
    0,
    ARRAY['seaborn', 'visualization', 'plotting']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0004-0002-0001-000000000001',
    't0000000-0000-0004-0002-000000000001',
    'Choosing the Right Visualization',
    'Matching chart types to data types and analytical questions.',
    'BEGINNER', 0, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0004-0002-0001-000000000001',
    'x0000000-0004-0002-0001-000000000001',
    'The Art of Choosing Charts',
    'TEXT',
    '{"body": "The right visualization can reveal insights hidden in raw data.\n\n## Chart Selection Guide\n\n| Data Question | Chart Type | Seaborn Function |\n|---|---|---|\n| Distribution of one variable | Histogram / KDE | `sns.histplot()` / `sns.kdeplot()` |\n| Compare categories | Bar chart | `sns.barplot()` / `sns.countplot()` |\n| Relationship between two variables | Scatter plot | `sns.scatterplot()` |\n| Trend over time | Line chart | `sns.lineplot()` |\n| Correlation matrix | Heatmap | `sns.heatmap()` |\n| Distribution comparison | Box / Violin | `sns.boxplot()` / `sns.violinplot()` |\n\n## Example: Correlation Heatmap\n```python\nimport seaborn as sns\nimport matplotlib.pyplot as plt\n\ncorr = df.corr()\nsns.heatmap(corr, annot=True, cmap=''coolwarm'', center=0)\nplt.title(''Feature Correlations'')\nplt.tight_layout()\nplt.show()\n```\n\n## Best Practices\n1. **Start simple** — don''t over-decorate\n2. **Label everything** — title, axes, legend\n3. **Use color purposefully** — highlight, don''t confuse\n4. **Remove chartjunk** — no unnecessary gridlines or 3D effects", "summary": "Choose visualizations based on your data question: histograms for distributions, scatter for relationships, heatmaps for correlations."}',
    0, 15
);


-- ==================== COURSE 5: Generative AI and Prompt Engineering ====================
INSERT INTO courses (id, title, slug, description, short_description, difficulty, category, tags, instructor_id, published, rating, enrollment_count, estimated_hours)
VALUES (
    'c0000000-0000-0000-0000-000000000005',
    'Generative AI and Prompt Engineering',
    'generative-ai-prompt-engineering',
    'Understand and harness the power of generative AI. Learn how large language models work, master prompt engineering techniques, build RAG systems, use function calling, and create AI agents. Covers GPT-4, Claude, Gemini, and open-source models.',
    'Master LLMs, prompt engineering, RAG, and AI agents',
    'INTERMEDIATE',
    'Artificial Intelligence',
    ARRAY['Generative AI', 'LLM', 'Prompt Engineering', 'RAG', 'AI Agents', 'GPT-4'],
    'a0000000-0000-0000-0000-000000000001',
    TRUE, 4.9, 22100, 30
);

-- Module 5.1: Understanding LLMs
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0005-000000000001',
    'c0000000-0000-0000-0000-000000000005',
    'How Large Language Models Work',
    'Demystify the technology behind ChatGPT, Claude, and Gemini.',
    0,
    ARRAY['Explain how LLMs generate text (next-token prediction)', 'Understand training: pretraining, fine-tuning, RLHF', 'Compare different model families and their strengths'],
    120
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0005-0001-000000000001',
    'm0000000-0000-0000-0005-000000000001',
    'LLM Architecture and Training',
    'From pretraining to RLHF — how models learn language.',
    0,
    ARRAY['LLM', 'pretraining', 'RLHF', 'fine-tuning']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0005-0001-0001-000000000001',
    't0000000-0000-0005-0001-000000000001',
    'Next-Token Prediction',
    'The fundamental mechanism by which LLMs generate coherent text.',
    'INTERMEDIATE', 0, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0005-0001-0001-000000000001',
    'x0000000-0005-0001-0001-000000000001',
    'How LLMs Generate Text',
    'TEXT',
    '{"body": "Large language models are, at their core, next-token predictors trained on vast amounts of text.\n\n## The Core Mechanism\nGiven a sequence of tokens, predict the most likely next token:\n```\nInput:  \"The cat sat on the\"\nOutput: \"mat\" (probability: 0.15)\n        \"floor\" (probability: 0.12)\n        \"roof\" (probability: 0.08)\n        ...\n```\n\n## Training Pipeline\n\n### Stage 1: Pretraining\n- Train on massive text corpora (internet, books, code)\n- Objective: Predict next token (causal LM) or fill masks (masked LM)\n- Result: Base model with broad knowledge\n\n### Stage 2: Supervised Fine-Tuning (SFT)\n- Train on curated instruction-response pairs\n- Model learns to follow instructions and be helpful\n\n### Stage 3: RLHF (Reinforcement Learning from Human Feedback)\n- Humans rank model outputs by quality\n- Train a reward model on these preferences\n- Use PPO to optimize the LLM against the reward model\n- Result: Model is helpful, harmless, and honest\n\n## Temperature and Sampling\n- **Temperature = 0:** Deterministic (always pick highest probability)\n- **Temperature = 1:** Sample from full distribution\n- **Temperature > 1:** More random/creative\n- **Top-p (nucleus sampling):** Sample from the smallest set of tokens whose cumulative probability exceeds p", "summary": "LLMs predict the next token; they are trained via pretraining on text, fine-tuning on instructions, and RLHF for alignment."}',
    0, 15
);

-- Module 5.2: Prompt Engineering
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0005-000000000002',
    'c0000000-0000-0000-0000-000000000005',
    'Prompt Engineering Techniques',
    'Master the art and science of crafting effective prompts.',
    1,
    ARRAY['Apply zero-shot, few-shot, and chain-of-thought prompting', 'Use system prompts and role-based prompting', 'Build structured output with JSON mode and function calling'],
    180
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0005-0002-000000000001',
    'm0000000-0000-0000-0005-000000000002',
    'Core Prompting Techniques',
    'From zero-shot to chain-of-thought.',
    0,
    ARRAY['prompting', 'few-shot', 'chain-of-thought']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0005-0002-0001-000000000001',
    't0000000-0000-0005-0002-000000000001',
    'Chain-of-Thought Prompting',
    'Unlocking reasoning capabilities by asking models to think step by step.',
    'INTERMEDIATE', 0, 0.8, 20
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0005-0002-0001-000000000001',
    'x0000000-0005-0002-0001-000000000001',
    'Chain-of-Thought Prompting',
    'TEXT',
    '{"body": "Chain-of-Thought (CoT) prompting dramatically improves LLM performance on reasoning tasks.\n\n## The Problem\nLLMs often fail at multi-step reasoning:\n```\nQ: If a store has 45 apples and sells 3 bags of 12, how many are left?\nA: 12  ❌ (wrong — just grabbed a number)\n```\n\n## The Solution: Think Step by Step\n```\nQ: If a store has 45 apples and sells 3 bags of 12, how many are left?\nA: Let me think step by step.\n   1. The store starts with 45 apples\n   2. Each bag has 12 apples, and 3 bags are sold\n   3. Apples sold: 3 × 12 = 36\n   4. Apples remaining: 45 - 36 = 9\n   The answer is 9. ✅\n```\n\n## Techniques\n\n### Zero-Shot CoT\nJust add: **\"Let''s think step by step\"**\n\n### Few-Shot CoT\nProvide examples with reasoning chains before asking the question.\n\n### Self-Consistency\n1. Generate multiple CoT reasoning paths\n2. Take the majority vote on the final answer\n3. Significantly improves accuracy\n\n### Tree of Thought\nExplore multiple reasoning branches and evaluate which is most promising.\n\n## When to Use CoT\n- Math and logic problems\n- Multi-step reasoning\n- Complex decision-making\n- Code debugging and analysis", "summary": "Chain-of-thought prompting adds step-by-step reasoning, dramatically improving LLM accuracy on complex tasks."}',
    0, 15
);

-- Module 5.3: RAG and AI Agents
INSERT INTO modules (id, course_id, title, description, order_index, learning_objectives, estimated_minutes)
VALUES (
    'm0000000-0000-0000-0005-000000000003',
    'c0000000-0000-0000-0000-000000000005',
    'RAG Systems and AI Agents',
    'Build production-grade retrieval-augmented generation and autonomous agents.',
    2,
    ARRAY['Build a RAG pipeline with vector databases', 'Implement tool-using AI agents', 'Evaluate and improve RAG system quality'],
    200
);

INSERT INTO topics (id, module_id, title, description, order_index, tags)
VALUES (
    't0000000-0000-0005-0003-000000000001',
    'm0000000-0000-0000-0005-000000000003',
    'Retrieval-Augmented Generation',
    'Grounding LLMs with external knowledge.',
    0,
    ARRAY['RAG', 'vector database', 'embeddings', 'retrieval']
);

INSERT INTO concepts (id, topic_id, title, description, difficulty, order_index, mastery_threshold, estimated_minutes)
VALUES (
    'x0000000-0005-0003-0001-000000000001',
    't0000000-0000-0005-0003-000000000001',
    'RAG Pipeline Architecture',
    'How to combine retrieval with generation for accurate, grounded responses.',
    'ADVANCED', 0, 0.8, 25
);

INSERT INTO learning_units (id, concept_id, title, content_type, content, order_index, duration_minutes)
VALUES (
    'u0000000-0005-0003-0001-000000000001',
    'x0000000-0005-0003-0001-000000000001',
    'Building RAG Systems',
    'TEXT',
    '{"body": "Retrieval-Augmented Generation (RAG) solves the hallucination problem by grounding LLMs in real data.\n\n## The RAG Pipeline\n\n### Indexing Phase (Offline)\n1. **Load documents:** PDFs, web pages, databases\n2. **Chunk:** Split documents into 500-1000 token chunks\n3. **Embed:** Convert chunks to vectors using embedding models\n4. **Store:** Index vectors in a vector database (Pinecone, Weaviate, ChromaDB)\n\n### Query Phase (Online)\n1. **Embed query:** Convert user question to a vector\n2. **Retrieve:** Find top-k most similar chunks via cosine similarity\n3. **Augment prompt:** Insert retrieved chunks into the LLM prompt\n4. **Generate:** LLM answers using the provided context\n\n## Example Architecture\n```python\n# Indexing\nchunks = text_splitter.split(documents)\nembeddings = embed_model.encode(chunks)\nvector_store.upsert(chunks, embeddings)\n\n# Querying\nquery_embedding = embed_model.encode(user_question)\nrelevant_chunks = vector_store.search(query_embedding, top_k=5)\n\nprompt = f\"\"\"Answer based on this context:\n{relevant_chunks}\n\nQuestion: {user_question}\"\"\"\n\nanswer = llm.generate(prompt)\n```\n\n## Key Decisions\n- **Chunk size:** Smaller = more precise, larger = more context\n- **Embedding model:** OpenAI, Cohere, or open-source (BGE, E5)\n- **Retrieval strategy:** Dense, sparse (BM25), or hybrid\n- **Reranking:** Use a cross-encoder to re-score retrieved results", "summary": "RAG grounds LLMs by retrieving relevant document chunks from a vector database and including them in the prompt context."}',
    0, 20
);

-- ==================== QUESTIONS (sample for Course 1) ====================
INSERT INTO questions (id, concept_id, question_type, question_text, correct_answer, explanation, difficulty, order_index) VALUES
('q0000000-0000-0000-0001-000000000001', 'x0000000-0001-0001-0001-000000000001', 'MULTIPLE_CHOICE',
 'Which of the following best describes machine learning?',
 'A field of AI that enables systems to learn and improve from experience without explicit programming',
 'Machine learning is specifically about learning from data/experience, distinguishing it from traditional rule-based programming.',
 'BEGINNER', 0),

('q0000000-0000-0000-0001-000000000002', 'x0000000-0001-0001-0001-000000000002', 'MULTIPLE_CHOICE',
 'A dataset of emails labeled as "spam" or "not spam" is used to train a classifier. What type of machine learning is this?',
 'Supervised Learning',
 'Since the data has labels (spam/not spam), the algorithm learns the mapping from input to output — this is supervised learning.',
 'BEGINNER', 0),

('q0000000-0000-0000-0001-000000000003', 'x0000000-0001-0002-0001-000000000001', 'SHORT_ANSWER',
 'In simple linear regression y = mx + b, what does the variable m represent?',
 'The slope or weight/coefficient of the input feature',
 'The slope m determines how much y changes for a unit change in x. It is also called the weight or coefficient.',
 'BEGINNER', 0),

('q0000000-0000-0000-0001-000000000004', 'x0000000-0001-0003-0001-000000000001', 'MULTIPLE_CHOICE',
 'If a cancer detection model has high recall but low precision, what does this mean?',
 'It catches most cancer cases but also has many false alarms',
 'High recall means few false negatives (misses), but low precision means many false positives (false alarms).',
 'INTERMEDIATE', 0),

('q0000000-0000-0000-0002-000000000001', 'x0000000-0002-0001-0001-000000000001', 'TRUE_FALSE',
 'A single perceptron can learn the XOR function.',
 'False',
 'XOR is not linearly separable, so a single perceptron cannot learn it. This was famously shown by Minsky and Papert in 1969.',
 'BEGINNER', 0),

('q0000000-0000-0000-0003-000000000001', 'x0000000-0003-0002-0001-000000000001', 'SHORT_ANSWER',
 'In the transformer attention formula, what is the purpose of dividing by the square root of d_k?',
 'To prevent the dot products from becoming too large, which would push softmax into regions with very small gradients',
 'Without scaling, large dot products cause softmax to produce near-one-hot distributions, leading to vanishing gradients.',
 'ADVANCED', 0),

('q0000000-0000-0000-0005-000000000001', 'x0000000-0005-0002-0001-000000000001', 'MULTIPLE_CHOICE',
 'What simple phrase can you add to a prompt to trigger chain-of-thought reasoning?',
 'Let''s think step by step',
 'This is known as zero-shot chain-of-thought prompting, introduced by Kojima et al. in 2022.',
 'BEGINNER', 0);
