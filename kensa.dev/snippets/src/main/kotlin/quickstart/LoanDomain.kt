package quickstart

enum class LoanStatus { Approved, Declined }

data class LoanResult(val status: LoanStatus, val reference: String)

data class Applicant(val name: String, val creditScore: Int, val amount: Int)

class LoanService {
    fun process(applicant: Applicant): LoanResult =
        if (applicant.creditScore >= 600) LoanResult(LoanStatus.Approved, "LN-1234")
        else LoanResult(LoanStatus.Declined, "LN-0000")
}
