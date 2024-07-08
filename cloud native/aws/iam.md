## IAM (Identity and Access Management)

AWS IAM은 AWS 리소스에 대한 접근(access)을 제어하는 웹 서비스

리소스를 사용하도록 인증 및 권한 부여된 대상을 제어


## IAM Workflow

<img src="./images/iam workflow.png" alt="iam workflow" />

1. 실제 사용자 또는 애플리케이션 -> AWS를 통해 인증
    - AWS 계정에서 신뢰하는 보안 주체와 로그인 보안 인증 정보를 일치시키는 방식으로 인증 제공
    - 보안 주체: IAM 사용자, Federation 사용자, IAM 역할 또는 애플리케이션
2. 보안 주체에게 접근 권한 부여 요청 -> 접근 권한 부여
3. 사용자 -> 서비스 접근
    - 인증 요청이 해당 서비스로 전송되어 인증된 사용자 목록에 ID가 있는지, 부여된 접근 수준을 제어하기 위해 정책 확인
4. 인증된 후 보안 주체가 리소스에 접근하여 작업 수행

## Terms

<img src="./images/iam terms.png" alt="iam term">

**IAM 리소스**
- IAM user
- IAM role
- IAM group
- IAM policy
- OIDC provider
- etc

**IAM 엔터티**
- AWS가 인증에 사용하는 IAM 리소스
- IAM user
- IAM role

**IAM 자격 증명**
- 정책에서 권한을 부여받아 작업을 수행하고 리소스에 접근할 수 있는 IAM 리소스

**Principal(보안 주체)** 
- AWS 계정 루트 사용자
- IAM 사용자 또는 IAM 역할을 사용하여 로그인하고 AWS에 요청하는 사람 또는 애플리케이션, Federation 포함

**루트 사용자**

AWS 계정을 생성할 때 만들어진 단일 로그인 ID

해당 계정의 모든 AWAS 서비스 및 리소스에 대한 완전한 접근 권한이 있는 계정

[루트 사용자 인증이 필요한 작업](https://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/root-user-tasks.html)이 아닌 경우 루트 사용자 사용 X


