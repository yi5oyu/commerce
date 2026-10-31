<!--
PR 제목은 커밋 메시지 규칙(Conventional Commits)을 따릅니다. Squash merge 시 그대로 main의 커밋 메시지가 됩니다.
  예) feat(member): 회원가입 API 추가
      fix(auth): 만료 토큰에 403이 반환되는 문제 수정

본문은 이 브랜치의 ep 파일에서 채웁니다.
ep 목록: `git log main..{branch}`의 `ep:` 트레일러 → `docs/plans/done/`
-->

## 목적

<!-- 이 브랜치가 달성한 것 한두 문장. ep들의 Purpose를 묶어서. -->

## 변경 내용

<!-- ep마다 한 줄: ep 링크 — 그 ep의 Purpose 한 줄. 파일 목록이 아니라 동작 기준으로. -->

- [ep-YYYYMMDD-slug](docs/plans/done/ep-YYYYMMDD-slug.md) — 

## 확인 방법

<!-- 리뷰어(또는 미래의 나)가 그대로 따라 할 수 있게. 실행한 명령과 결과.
     브랜치가 걸친 폴더의 `AGENTS.md` `## Commands`를 실행하고, 걸치지 않은 폴더 줄은 지운다.
     ep별 Acceptance Criteria와 근거는 각 ep 파일에 체크되어 있다 — 수동 확인이 필요한 것만 여기 단계로 적는다. -->

- backend (`./gradlew build`): 
- frontend (`npm run lint` · `npx tsc --noEmit` · `npm run build`): 

## 관련 문서

<!-- 해당 없는 항목은 지웁니다. -->

- 이슈: closes #

## 체크리스트

- [ ] 변경한 폴더의 `AGENTS.md` 명령어(빌드·테스트·린트)를 로컬에서 실행했다
- [ ] 이 브랜치의 ep가 모두 `[Done]`이고 `docs/plans/done/`에 있다
- [ ] 비밀값(키, 비밀번호, `.env`)이 포함되지 않았다
- [ ] 설계 결정은 해당 ep의 `## Decisions`에 기록했다 (프로젝트 전체에 걸친 규칙이면 `AGENTS.md`)
- [ ] 명령어·구조·규칙이 바뀌었다면 해당 `AGENTS.md`와 문서를 고쳤다

## 참고 사항

<!-- 선택. ep의 Decisions · Risk 중 리뷰 때 봐야 할 것, 남은 작업, 알려진 한계. 없으면 지웁니다. -->
