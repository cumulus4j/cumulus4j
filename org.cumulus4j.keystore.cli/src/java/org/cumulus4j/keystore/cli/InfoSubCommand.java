package org.cumulus4j.keystore.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.cumulus4j.keystore.prop.Property;

/**
 * <p>
 * {@link SubCommand} implementation for showing various infos about a key-store.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InfoSubCommand extends SubCommandWithKeyStoreWithAuth
{
	@Override
	public String getSubCommandName() {
		return "info";
	}

	@Override
	public String getSubCommandDescription() {
		return "Display infos about the key-store.";
	}

	@Override
	public void run() throws Exception
	{
		System.out.println("Key store: " + getKeyStoreFile().getCanonicalPath());
		System.out.println();

		if (getKeyStore().isEmpty()) {
			System.out.println("This key store is empty!");
			System.out.println();
			return;
		}

		System.out.println("Size of master key: " + getKeyStore().getMasterKeySize(getAuthUserName(), getAuthPasswordAsCharArray()) + " bit");
		System.out.println();

		Set<String> users = getKeyStore().getUsers(getAuthUserName(), getAuthPasswordAsCharArray());
		System.out.println("Users: ");
		if (users.isEmpty())
			System.out.println("  --- EMPTY ---");

		for (String user : users) {
			System.out.println("  " + user);
		}
		System.out.println();

		SortedSet<Long> keyIDs = getKeyStore().getKeyIDs(getAuthUserName(), getAuthPasswordAsCharArray());
		List<Long[]> keyIDIntervals = new LinkedList<Long[]>();
		{
			Long[] keyIDInterval = null;
			for (Long keyID : keyIDs) {
				if (keyIDInterval != null && keyID == keyIDInterval[1] + 1)
					keyIDInterval[1] = keyID;
				else {
					keyIDInterval = new Long[2];
					keyIDIntervals.add(keyIDInterval);
					keyIDInterval[0] = keyID;
					keyIDInterval[1] = keyID;
				}
			}
		}

		System.out.println("Key IDs:");
		if (keyIDIntervals.isEmpty())
			System.out.println("  --- EMPTY ---");

		for (Long[] keyIDInterval : keyIDIntervals) {
			System.out.println("  " + keyIDInterval[0] + "..." + keyIDInterval[1]);
		}
		System.out.println();

		System.out.println("Properties:");
		SortedSet<Property<?>> properties = getKeyStore().getProperties(getAuthUserName(), getAuthPasswordAsCharArray());
		if (properties.isEmpty())
			System.out.println("  --- EMPTY ---");

		// We do not output the details here! 'info' should give an overview only. Details can be obtained via other sub-commands.
//		for (Property<?> property : properties) {
//			System.out.println("  " + property.getName() + " (" + property.getClass().getName() + ") = " + property.getValue());
//		}
		for (Property<?> property : properties) {
			System.out.println("  " + property.getName() + " (" + property.getClass().getName() + ", " + property.getValueEncoded().length + " Byte)");
		}

		System.out.println();
	}

}
