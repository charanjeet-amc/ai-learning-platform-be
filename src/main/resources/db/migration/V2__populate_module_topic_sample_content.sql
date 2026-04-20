-- Populate modules with description and learning objectives where missing

UPDATE modules
SET description = 'This module covers the fundamentals and key principles of ' || title ||
                  '. You will explore core ideas, practical applications, and build a solid understanding of the subject matter.',
    updated_at = NOW()
WHERE description IS NULL OR description = '';

UPDATE modules
SET learning_objectives = ARRAY[
    'Understand the core concepts of ' || title,
    'Apply key principles in practical scenarios',
    'Analyze and evaluate problems related to ' || title,
    'Build foundational skills for advanced study'
]
WHERE learning_objectives IS NULL OR array_length(learning_objectives, 1) IS NULL;

-- Populate topics with tags and estimated time where missing

UPDATE topics
SET estimated_time_minutes = 15 + (order_index * 5),
    updated_at = NOW()
WHERE estimated_time_minutes IS NULL;

UPDATE topics
SET tags = ARRAY[
    split_part(title, ' ', 1),
    CASE WHEN order_index = 0 THEN 'introduction'
         WHEN order_index = 1 THEN 'fundamentals'
         ELSE 'advanced' END
]
WHERE tags IS NULL OR array_length(tags, 1) IS NULL;

-- Also populate topic descriptions where missing
UPDATE topics
SET description = 'Explore ' || title || ' in depth, covering essential techniques and building practical understanding.'
WHERE description IS NULL OR description = '';
