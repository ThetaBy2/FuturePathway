#!/usr/bin/env bash
# Future Pathway — end-to-end curl walkthrough (assumes API on :8080).
set -euo pipefail
API=${API:-http://localhost:8080/api}

echo "== 1. Register student + company =="
STU_TOKEN=$(curl -s -X POST "$API/auth/register/student" -H 'Content-Type: application/json' -d '{
  "name":"Asha Rao","email":"asha@example.com","password":"Passw0rd1",
  "rollNo":"CSE2026001","branch":"CSE","batchYear":2026,"cgpa":8.6,
  "resumeText":"Java developer. Built a Spring Boot placement portal with JWT and PostgreSQL."}' | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')
echo "student token acquired"

CMP_TOKEN=$(curl -s -X POST "$API/auth/register/company" -H 'Content-Type: application/json' -d '{
  "contactName":"Ravi HR","email":"hr@acme.io","password":"Passw0rd1",
  "companyName":"Acme Tech","industryDomain":"SaaS"}' | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')
echo "company token acquired"

echo "== 2. Student adds skill + project =="
curl -s -X POST "$API/students/me/skills" -H "Authorization: Bearer $STU_TOKEN" -H 'Content-Type: application/json' \
  -d '{"skillId":1,"proficiencyLevel":4}' | head -c 300; echo
curl -s -X POST "$API/students/me/projects" -H "Authorization: Bearer $STU_TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Placement portal","description":"Spring Boot + JWT + PostgreSQL","techStack":"Java, Spring Boot, PostgreSQL"}' | head -c 300; echo

echo "== 3. Company posts a job =="
JOB_ID=$(curl -s -X POST "$API/jobs" -H "Authorization: Bearer $CMP_TOKEN" -H 'Content-Type: application/json' -d '{
  "title":"Backend Developer","description":"Java microservices role","jobDomain":"Engineering",
  "minCgpa":7.0,"maxBacklogs":1,"eligibleBranches":["CSE","IT"],"packageLpa":12.0,
  "deadline":"2026-12-31","skills":[{"skillId":1,"requiredProficiency":4},{"skillId":3,"requiredProficiency":3}]}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
echo "job id: $JOB_ID"

echo "== 4. Student applies (Gemini match is computed + stored) =="
curl -s -X POST "$API/jobs/$JOB_ID/apply" -H "Authorization: Bearer $STU_TOKEN" | python3 -m json.tool

echo "== 5. AI: analyse profile + roadmap + match =="
curl -s -X POST "$API/ai/analyze-me" -H "Authorization: Bearer $STU_TOKEN" | python3 -m json.tool | head -40
curl -s -X POST "$API/ai/roadmap" -H "Authorization: Bearer $STU_TOKEN" -H 'Content-Type: application/json' \
  -d '{"targetRole":"Backend Developer"}' | python3 -m json.tool | head -40
curl -s "$API/ai/match/$JOB_ID" -H "Authorization: Bearer $STU_TOKEN" | python3 -m json.tool

echo "== 6. Portal: trending + dashboard (login as seeded admin/college for dashboard) =="
curl -s "$API/portal/trending?limit=5" -H "Authorization: Bearer $STU_TOKEN" | python3 -m json.tool
echo DONE
