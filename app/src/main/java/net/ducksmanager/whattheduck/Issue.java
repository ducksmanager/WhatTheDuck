package net.ducksmanager.whattheduck;



public class Issue {
	public static final String BAD_CONDITION="mauvais";
	public static final String NOTSOGOOD_CONDITION="moyen";
	public static final String GOOD_CONDITION="bon";
	public static final String NO_CONDITION="indefini";

	private final String issueNumber;
	public static enum IssueCondition {BAD_CONDITION, NOTSOGOOD_CONDITION, GOOD_CONDITION, NO_CONDITION}
	private IssueCondition issueCondition;

	public Issue(String issuenumber, String issueCondition) {
		super();
		this.issueNumber = issuenumber;
		this.issueCondition=issueConditionStrToIssueCondition(issueCondition);
	}
	
	public Issue(String issuenumber, IssueCondition issueCondition) {
		super();
		this.issueNumber = issuenumber;
		this.issueCondition = issueCondition;
	}

	public String getIssueNumber() {
		return issueNumber;
	}

	public IssueCondition getIssueCondition() {
		return issueCondition;
	}

	public void setIssueCondition(IssueCondition issueCondition) {
		this.issueCondition = issueCondition;
	}
	
	public static IssueCondition issueConditionStrToIssueCondition(String issueConditionStr) {
		if (issueConditionStr == null || issueConditionStr.equals(NO_CONDITION))
			return IssueCondition.NO_CONDITION;
		else if (issueConditionStr.equals(BAD_CONDITION))
			return IssueCondition.BAD_CONDITION;
		else if (issueConditionStr.equals(NOTSOGOOD_CONDITION))
			return IssueCondition.NOTSOGOOD_CONDITION;
		else if (issueConditionStr.equals(GOOD_CONDITION))
			return IssueCondition.GOOD_CONDITION;
		return IssueCondition.NO_CONDITION;
	}
	
	
	private static String issueConditionToIssueConditionStr(IssueCondition issueCondition) {
		if (issueCondition == null || issueCondition.equals(IssueCondition.NO_CONDITION))
			return NO_CONDITION;
		else if (issueCondition.equals(IssueCondition.BAD_CONDITION))
			return BAD_CONDITION;
		else if (issueCondition.equals(IssueCondition.NOTSOGOOD_CONDITION))
			return NOTSOGOOD_CONDITION;
		else if (issueCondition.equals(IssueCondition.GOOD_CONDITION))
			return GOOD_CONDITION;
		return NO_CONDITION;
	}
	
	public static int issueConditionToResourceId(IssueCondition issueCondition) {
		if (issueCondition == null || issueCondition.equals(IssueCondition.NO_CONDITION))
			return R.drawable.condition_none;
		else if (issueCondition.equals(IssueCondition.BAD_CONDITION))
			return R.drawable.condition_bad;
		else if (issueCondition.equals(IssueCondition.NOTSOGOOD_CONDITION))
			return R.drawable.condition_notsogood;
		else if (issueCondition.equals(IssueCondition.GOOD_CONDITION))
			return R.drawable.condition_good;
		return R.drawable.condition_none;
	}

	public String getIssueConditionStr() {
		return issueConditionToIssueConditionStr(issueCondition);
	}
	
}
