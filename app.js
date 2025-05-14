const express = require("express");
const bodyParser = require('body-parser'); // Добавляем middleware для парсинга тела запроса
var uuid = require('uuid');
var moment = require('moment');
const { Sequelize, DataTypes } = require("sequelize");

const sequelize = new Sequelize('NotesDB', 'root', '', {
    host: 'localhost',
    dialect: 'mysql'
});

const Notes = sequelize.define('Notes', {
    id: {
        type: DataTypes.UUID,
        allowNull: false,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true
    },
    text: {
        type: DataTypes.STRING,
        allowNull: false,
        defaultValue: ""
    },
    title: {
        type: DataTypes.STRING,
        allowNull: false,
        defaultValue: ""
    }
}, {
    timestamps: true,
    createdAt: 'createdAt',
    updatedAt: 'updatedAt'
});

const app = express();

// Добавляем middleware для парсинга application/x-www-form-urlencoded
app.use(bodyParser.urlencoded({ extended: true }));
// И для парсинга application/json
app.use(bodyParser.json());

app.get("/", async function(request, response) {
    try {
        await sequelize.authenticate();
        response.send('Connection has been established successfully.');
    } catch (error) {
        response.send('Unable to connect to the database: ' + error.message);
    }
});

app.get("/GetAllNotes", async function(request, response) {
    const notes = await Notes.findAll();
    response.json(notes);
});

app.get("/GetNoteById", async function(request, response) {
    const noteId = request.query.id;
    const note = await Notes.findByPk(noteId);
    response.json(note);
});

app.post("/CreateNote", async function(request, response) {
    // Теперь получаем данные из тела запроса (request.body), а не из query-параметров
    const { title, text } = request.body;
    const note = await Notes.create({ 
        title: title, 
        text: text,
        id: uuid.v4()
    });
    response.json(note.id);
});

app.post("/EditNote", async function(request, response) {
    // Аналогично получаем данные из тела запроса
    const { id, title, text } = request.body;
    const note = await Notes.findByPk(id);
    note.text = text;
    note.title = title;
    await note.save();
    response.json(note.id);
});

app.post("/DeleteNote", async function(request, response) {
    // Для удаления можно оставить query-параметр или переделать на body
    const noteId = request.query.id;
    await Notes.destroy({ where: { id: noteId } });
    response.json({ success: true });
});

app.listen(4000, () => {
    console.log('Server is running on port 4000');
});