package managment.view.swing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;

import managment.model.Client;
import managment.model.Purchase;

@RunWith(GUITestRunner.class)
public class ManagmentViewSwingTest extends AssertJSwingJUnitTestCase {

	private static final LocalDateTime TEST_DATE = LocalDate.of(2024, Month.JANUARY, 1).atStartOfDay();

	private ManagmentViewSwing managmentViewSwing;

	private FrameFixture window;

	@Override
	protected void onSetUp() {
		GuiActionRunner.execute(() -> {
			managmentViewSwing = new ManagmentViewSwing();
			return managmentViewSwing;
		});
		window = new FrameFixture(robot(), managmentViewSwing);
		window.show();
	}

	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Client Name"));
		window.textBox("clientNameBox").requireEnabled();
		window.label(JLabelMatcher.withText("Purchase Ammount"));
		window.textBox("purchaseAmmountBox").requireEnabled();

		window.button(JButtonMatcher.withText("Add New Client")).requireDisabled();
		window.button(JButtonMatcher.withText("Add Ammount")).requireDisabled();

		window.list("clientList");
		window.list("purchaseList");

		window.button(JButtonMatcher.withText("Delete Selected Client")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).requireDisabled();
		window.label("messageLable").requireText(" ");
	}

	@Test
	public void testWhenClientNameIsNonEmptyThenAddNewClientButtonShouldBeEnabled() {
		window.textBox("clientNameBox").enterText("testClient");
		window.button(JButtonMatcher.withText("Add New Client")).requireEnabled();

		window.textBox("clientNameBox").setText("");
		window.textBox("clientNameBox").enterText(" ");
		window.button(JButtonMatcher.withText("Add New Client")).requireDisabled();
	}

	@Test
	public void testDeleteClientButtonShouldBeEnabledOnlyWhenAClientIsSelected() {
		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));

		window.list("clientList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Client"));
		deleteButton.requireEnabled();
		window.list("clientList").clearSelection();
		deleteButton.requireDisabled();
	}

	@Test
	public void testWhenPurchaseAmmountIsNonEmptyAndClientIsSelectedThenAddPurchaseButtonShouldBeEnabled() {
		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		window.list("clientList").selectItem(0);

		window.textBox("purchaseAmmountBox").enterText("10.0");
		window.button(JButtonMatcher.withText("Add Ammount")).requireEnabled();

		window.textBox("purchaseAmmountBox").setText("");
		window.textBox("purchaseAmmountBox").enterText(" ");
		window.button(JButtonMatcher.withText("Add Ammount")).requireDisabled();

		window.list("clientList").clearSelection();
		window.button(JButtonMatcher.withText("Add Ammount")).requireDisabled();

		window.textBox("purchaseAmmountBox").enterText("10.0");
		window.button(JButtonMatcher.withText("Add Ammount")).requireDisabled();

		window.list("clientList").selectItem(0);
		window.button(JButtonMatcher.withText("Add Ammount")).requireEnabled();

	}

	@Test
	public void testDeletePurchaseButtonShouldBeDisabledWhenAClientAndAPurchaseIsNotSelected() {
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Purchase"));

		deleteButton.requireDisabled();

		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));

		window.list("clientList").selectItem(0);
		deleteButton.requireDisabled();

		GuiActionRunner
				.execute(() -> managmentViewSwing.getListPurchaseModel().addElement(new Purchase(1, TEST_DATE, 10.0)));

		window.list("clientList").clearSelection();
		window.list("purchaseList").selectItem(0);
		deleteButton.requireDisabled();

		window.list("clientList").selectItem(0);
		deleteButton.requireEnabled();
	}

	@Test
	public void testDeletePurchaseButtonShouldBeEnabledWhenAClientAndAPurchaseIsSelected() {
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Purchase"));

		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		GuiActionRunner
				.execute(() -> managmentViewSwing.getListPurchaseModel().addElement(new Purchase(TEST_DATE, 10.0)));

		window.list("clientList").selectItem(0);
		window.list("purchaseList").selectItem(0);
		deleteButton.requireEnabled();
	}

	@Test
	public void testShowAllClientsShouldAddClientDescriptionsToTheList() {
		Client client1 = new Client(1, "client1");
		Client client2 = new Client(2, "client2");

		GuiActionRunner.execute(() -> managmentViewSwing.showAllClients(asList(client1, client2)));

		String[] listContents = window.list("clientList").contents();
		assertThat(listContents).containsExactly(client1.toString(), client2.toString());
	}

	@Test
	public void testShowClientNotFoundErrorShouldShowMessageInMessageLable() {
		Client client = new Client(1, "client");
		GuiActionRunner.execute(
				() -> managmentViewSwing.showClientNotFoundError("Client [id=1, name=client] not found", client));
		window.label("messageLable").requireText("Client [id=1, name=client] not found");
	}

	@Test
	public void testClientAddedShouldAddTheClientToTheListAndResetTheMessageLabel() {
		Client client = new Client(1, "client");
		GuiActionRunner.execute(() -> managmentViewSwing.clientAdded(new Client(1, "client")));
		String[] listContents = window.list("clientList").contents();
		assertThat(listContents).containsExactly(client.toString());
		window.label("messageLable").requireText(" ");
	}

	@Test
	public void testStudentRemovedShouldRemoveTheStudentFromTheListAndResetTheErrorLabel() {
		Client toRemove = new Client(1, "toRemove");
		Client client = new Client(2, "client");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel = managmentViewSwing.getListClientsModel();
			listClientModel.addElement(toRemove);
			listClientModel.addElement(client);
		});

		GuiActionRunner.execute(() -> managmentViewSwing.clientRemoved(new Client(1, "toRemove")));

		String[] listContents = window.list("clientList").contents();
		assertThat(listContents).containsExactly(client.toString());
		window.label("messageLable").requireText(" ");
	}

	@Test
	public void testShowAllPurchaseShouldAddPurchaseDescriptionsToTheList() {
		Purchase purchase1 = new Purchase(1, TEST_DATE, 10.0);
		Purchase purchase2 = new Purchase(2, TEST_DATE, 5.0);

		GuiActionRunner.execute(() -> managmentViewSwing.showAllPurchases(asList(purchase1, purchase2)));

		String[] listContents = window.list("purchaseList").contents();
		assertThat(listContents).containsExactly(purchase1.toString(), purchase2.toString());
	}
	
	@Test
	public void testShowPurchaseNotFoundErrorShouldShowMessageInMessageLable() {
		Purchase purchase = new Purchase(1, TEST_DATE, 10.0);
		GuiActionRunner.execute(
				() -> managmentViewSwing.showPurchaseNotFoundError(" ", purchase));
		window.label("messageLable").requireText("Purchase [id=1, orderDate=2024-01-01T00:00, amount=10.0] not found");
	}
}