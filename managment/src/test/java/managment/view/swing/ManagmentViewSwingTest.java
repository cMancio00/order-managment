package managment.view.swing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

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
import static org.assertj.core.api.Assertions.assertThat;

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
		window.label(JLabelMatcher.withText(" "));
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
		GuiActionRunner.execute(() -> 
			managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		
		window.list("clientList").selectItem(0);
		JButtonFixture deleteButton =
				window.button(JButtonMatcher.withText("Delete Selected Client"));
				deleteButton.requireEnabled();
				window.list("clientList").clearSelection();
				deleteButton.requireDisabled();
	}
	
	@Test
	public void testWhenPurchaseAmmountIsNonEmptyAndClientIsSelectedThenAddPurchaseButtonShouldBeEnabled() {
		GuiActionRunner.execute(() -> 
		managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
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
				.execute(() -> managmentViewSwing.getListPurchaseModel().addElement(new Purchase(TEST_DATE, 10.0)));

		window.list("clientList").clearSelection();
		window.list("purchaseList").selectItem(0);
		deleteButton.requireDisabled();

		window.list("clientList").selectItem(0);
		deleteButton.requireEnabled();
	}
	
	@Test
	public void testDeletePurchaseButtonShouldBeEnabledWhenAClientAndAPurchaseIsSelected() {
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Purchase"));

		GuiActionRunner.execute(() -> 
			managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		GuiActionRunner.execute(() -> 
			managmentViewSwing.getListPurchaseModel().addElement(new Purchase(TEST_DATE, 10.0)));

		window.list("clientList").selectItem(0);
		window.list("purchaseList").selectItem(0);
		deleteButton.requireEnabled();
	}
	
	
	@Test
	public void testShowAllClientsShouldAddClientDescriptionsToTheList() {
		Client client1 = new Client(1, "client1");
		Client client2 = new Client(2, "client2");
		
		GuiActionRunner.execute(() ->
		managmentViewSwing.showAllClients(asList(client1, client2))
		);
		
		String[] listContents = window.list("clientList").contents();
		assertThat(listContents)
		.containsExactly(client1.toString(), client2.toString());
	}
	
}
