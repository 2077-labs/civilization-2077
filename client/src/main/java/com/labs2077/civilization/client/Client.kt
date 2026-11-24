package com.labs2077.civilization.client

import com.labs2077.civilization.core.api.Command
import com.labs2077.civilization.core.api.Event
import com.labs2077.civilization.core.common.Result
import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.GameState
import com.labs2077.civilization.core.models.Player
import kotlinx.coroutines.flow.SharedFlow
import com.labs2077.civilization.core.api.Client as GameClient
import com.labs2077.civilization.core.api.Engine as GameEngine

class Client(val player: Player, private val engine: GameEngine) : GameClient {

    // TODO: Pobrać i zapisać referencję do wyliczonego stanu gry dla gracza.
    //       Ponieważ nie robimy event sourcingu, dopuszczamy bezpośrednie odniesienie się do silnika.
    val state: GameState =
        engine.state // TODO: To się jeszcze zmieni, przynajmniej jeżeli chodzi o sposób odczytu danych z silnika.
    val events: SharedFlow<Event> =
        engine.events // TODO: To się jeszcze zmieni, przynajmniej jeżeli chodzi o sposób odczytu danych z silnika.

    override fun dispatch(command: Command): Result<Collection<Event>, Error> {
        return engine.dispatch(player.id, command)
    }
}
