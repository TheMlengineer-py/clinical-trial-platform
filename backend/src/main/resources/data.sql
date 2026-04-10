-- ── Seed Data ────────────────────────────────────────────────────────────────
-- Runs automatically on startup via spring.sql.init.mode=always.
-- Provides realistic demo data so the assessor sees a populated UI immediately.

-- Studies — covering all four lifecycle states
INSERT INTO studies (title, status, max_enrollment, current_enrollment, eligibility_criteria, last_recruited_at, version)
VALUES
  ('BRCA Early Detection Trial',    'OPEN',     25, 18, 'age>18,condition=Breast cancer', NOW(), 0),
  ('Lung Biomarker Study Phase II',  'OPEN',     20,  8, 'age>18,condition=NSCLC',        NOW() - INTERVAL '2' DAY, 0),
  ('GI Immunotherapy Phase II',      'DRAFT',    30,  0, 'age>21,condition=Colorectal',   NULL, 0),
  ('Melanoma CAR-T Cell Therapy',    'CLOSED',   15, 15, 'age>18,condition=Melanoma',     NOW() - INTERVAL '7' DAY, 0),
  ('Pancreatic Biomarker Pilot',     'OPEN',     10,  3, 'age>40,condition=Pancreatic cancer', NOW() - INTERVAL '1' DAY, 0),
  ('Ovarian PARP Inhibitor Study',   'ARCHIVED', 20, 20, 'age>18,condition=Ovarian cancer', NOW() - INTERVAL '30' DAY, 0),
  ('Prostate Radiotherapy Boost',    'DRAFT',    40,  0, 'age>50,condition=Prostate cancer', NULL, 0);

-- Patients — mix of enrolled and unenrolled
INSERT INTO patients (name, age, condition, enrolled_study_id, recruited_at)
VALUES
  ('Sarah Thompson',   47, 'Breast cancer',     1, NOW() - INTERVAL '2'  HOUR),
  ('Marcus Delacroix', 62, 'NSCLC',             2, NOW() - INTERVAL '1'  DAY),
  ('Amara Nwosu',      55, 'Breast cancer',     NULL, NULL),
  ('James Kowalski',   71, 'Colorectal',        NULL, NULL),
  ('Linda Hartley',    58, 'NSCLC',             2, NOW() - INTERVAL '4'  DAY),
  ('David Okafor',     49, 'Pancreatic cancer', 5, NOW() - INTERVAL '6'  HOUR),
  ('Priya Shankar',    43, 'Breast cancer',     1, NOW() - INTERVAL '3'  DAY),
  ('Robert Mensah',    67, 'Prostate cancer',   NULL, NULL),
  ('Elena Vasquez',    52, 'Melanoma',          NULL, NULL),
  ('Tom Briggs',       61, 'NSCLC',             NULL, NULL);
