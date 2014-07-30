package net.ducksmanager.whattheduck;

public class AddIssue extends RetrieveTask {

    private static int progressBarId;

    private static IssueList issueList;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;

    public AddIssue(IssueList il, int progressBarId, String shortCountryAndPublication, Issue selectedIssue) {
        super(
            "&ajouter_numero"
            +"&pays_magazine="+shortCountryAndPublication
            +"&numero="+selectedIssue.getIssueNumber()
            +"&etat="+selectedIssue.getIssueConditionStr()
        );
        AddIssue.issueList = il;
        AddIssue.progressBarId = progressBarId;
        AddIssue.shortCountryAndPublication = shortCountryAndPublication;
        AddIssue.selectedIssue = selectedIssue;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(AddIssue.issueList, progressBarId, true);
    }

    @Override
    protected void onPostExecute(String response) {
        if (response.equals("OK")) {
            WhatTheDuck.wtd.info(AddIssue.issueList, R.string.confirmation_message__issue_inserted);
            WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, selectedIssue);
            issueList.show();
        }
        else {
            WhatTheDuck.wtd.alert(R.string.internal_error, R.string.internal_error__issue_insertion_failed);
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(AddIssue.issueList, progressBarId, false);
    }

}
