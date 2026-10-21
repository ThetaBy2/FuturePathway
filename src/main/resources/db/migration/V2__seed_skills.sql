-- Future Pathway · V2 — seed the admin-managed skill catalog.
-- Safe to re-run (ON CONFLICT DO NOTHING).

INSERT INTO skill (name, category, description) VALUES
    ('Java',               'TECHNICAL', 'Object-oriented programming with the Java platform'),
    ('Python',             'TECHNICAL', 'Scripting, automation and data workflows with Python'),
    ('SQL',                'TECHNICAL', 'Relational queries, joins, indexing and transactions'),
    ('Spring Boot',        'TECHNICAL', 'Production-grade Java microservices with Spring Boot'),
    ('React',              'TECHNICAL', 'Component-based UIs with React and modern JavaScript'),
    ('JavaScript',         'TECHNICAL', 'Core language of the web: ES6+, DOM, async patterns'),
    ('HTML/CSS',           'TECHNICAL', 'Semantic markup and responsive styling'),
    ('Git',                'TECHNICAL', 'Version control, branching and collaboration workflows'),
    ('Docker',             'TECHNICAL', 'Containerising and shipping applications'),
    ('Data Structures',    'TECHNICAL', 'Arrays, trees, graphs, hashing and complexity analysis'),
    ('Machine Learning',   'TECHNICAL', 'Classical ML: regression, trees, evaluation, scikit-learn'),
    ('REST APIs',          'TECHNICAL', 'Designing and consuming RESTful services'),
    ('Communication',      'SOFT',      'Clear written and verbal communication'),
    ('Teamwork',           'SOFT',      'Collaboration in cross-functional teams'),
    ('Problem Solving',    'SOFT',      'Structured debugging and analytical thinking'),
    ('Leadership',         'SOFT',      'Ownership, mentoring and decision making')
ON CONFLICT (name) DO NOTHING;
