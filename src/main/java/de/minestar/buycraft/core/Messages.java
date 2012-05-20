package de.minestar.buycraft.core;

public class Messages {
    // SERVERSHOPS
    public static String INFINITE_SHOP_DESTROY_ERROR = "Du kannst keine Servershops entfernen.";
    public static String INFINITE_SHOP_DESTROY_SUCCESS = "Servershop entfernt.";

    public static String INFINITE_SHOP_CREATE_ERROR = "Du kannst keine Servershops erstellen.";
    public static String INFINITE_SHOP_CREATE_SUCCESS = "Servershop erstellt.";
    public static String INFINITE_SHOP_CREATE_SUCCESS_COMPLETE = "Servershop vollständig erstellt.";

    // USERSHOPS
    public static String USER_SHOP_DESTROY_ERROR_THIS = "Du kannst diesen Usershops nicht entfernen.";
    public static String USER_SHOP_DESTROY_ERROR = "Du kannst keine Usershops entfernen.";
    public static String USER_SHOP_DESTROY_SUCCESS = "Usershop entfernt.";

    public static String USER_SHOP_DEACTIVATE_FIRST = "Der Usershop ist derzeit noch aktiviert. Bitte erst deaktivieren!";

    public static String USER_SHOP_ACTIVATED = "Usershop aktiviert.";
    public static String USER_SHOP_DEACTIVATED = "Usershop deaktiviert.";

    public static String USER_SHOP_CREATE_ERROR = "Du kannst keine Usershops erstellen.";
    public static String USER_SHOP_CREATE_FOR_OTHERS_ERROR = "Du kannst keine Usershops für andere Spieler erstellen.";
    public static String USER_SHOP_CREATE_SUCCESS = "Usershop erstellt.";
    public static String USER_SHOP_CREATE_SUCCESS_COMPLETE = "Usershop vollständig erstellt.";

    public static String USER_SHOP_INTERNAL_ERROR_0X00 = "Ein interner Fehler ist aufgetreten [0x00]. [Usershop expected, but not found]";
    public static String USER_SHOP_INTERNAL_ERROR_0X01 = "Ein interner Fehler ist aufgetreten [0x01]. [Could not create Usershop in DB]";
    public static String USER_SHOP_INTERNAL_ERROR_0X02 = "Ein interner Fehler ist aufgetreten [0x02]. [Could not activate Usershop in DB]";
    public static String USER_SHOP_INTERNAL_ERROR_0X03 = "Ein interner Fehler ist aufgetreten [0x03]. [Could not finish Usershop in DB]";
    public static String USER_SHOP_INTERNAL_ERROR_0X04 = "Ein interner Fehler ist aufgetreten [0x04]. [Could not remove Usershop in DB]";

    public static String ADMINS_MUST_SNEAK = "Admins müssen schleichen um einen fremden Usershop zu deaktivieren.";

    // ALIASES
    public static String NO_ALIASES_SET = "Bisher wurden keine Aliasnamen gesetzt.";
    public static String LIST_OF_ALIASES = "Liste der Aliasnamen:";

    public static String ALIAS_CREATED = "Der Aliasname wurde erfolgreich angelegt.";
    public static String ALIAS_CREATED_ERROR = "Der Aliasname konnte nicht angelegt werden!";

    public static String ALIAS_DELETED = "Der Aliasname wurde erfolgreich gelöscht.";
    public static String ALIAS_DELETED_ERROR = "Der Aliasname konnte nicht gelöscht werden!";

    public static String ALIAS_EXISTS_PLAYER = "Der Spieler hat schon einen Aliasnamen!";
    public static String ALIAS_EXISTS_ALIAS = "Der Aliasname ist schon vergeben!";
    public static String ALIAS_PLAYER_NOT_EXISTS = "Der Spieler hat keinen Aliasnamen!";

    // GENERAL
    public static String GIVE_CODE_TO_ADMIN = "Bitte diesen Code an einen Admin weitergeben!";
    public static String SHOP_CREATE_INFO = "Klicke mit dem Item in der Hand auf das Schild.";

    public static String SHOP_NOT_FINISHED = "Dieser Shop ist noch nicht fertig gestellt.";
    public static String SHOP_NOT_ACTIVATED = "Dieser Shop ist zur Zeit nicht aktiviert.";

    public static String TRY_AGAIN_OR_CONTACT_ADMIN = "Versuche es erneut oder kontaktiere einen Admin.";

    public static String ITEM_NOT_ALLOWED = "Dieses Item ist nicht zugelassen.";
    public static String WRONG_SYNTAX = "Unzulässige Syntax.";

    public static String NO_ITEM_IN_HAND = "Du musst das gewünschte Item in die Hand nehmen.";
    public static String DESTROY_SIGN_FIRST = "Das Schild muss zuerst entfernt werden!";
    public static String CREATE_CHEST_FIRST = "Du musst zuerst eine Kiste erstellen!";
    public static String DOUBLE_CHESTS_NOT_ALLOWED = "Doppelkisten sind nicht erlaubt!";
}
